#!/usr/local/bin/groovy
def nexusUser = "admin";
def nexusPassword = "admin123";
def artifactFolderURL = "http://" + nexusUser + ":" + nexusPassword + "@localhost:8081/service/rest/repository/browse/maven-releases/sim/CurrencyConverter/";
def store = 10
def artifactURL = "http://" + nexusUser + ":" + nexusPassword + "@localhost:8081/service/rest/beta/search/assets?version="
def deleteAPI = "http://localhost:8081/service/rest/beta/assets/"
def authString  = "${nexusUser}:${nexusPassword}".getBytes().encodeBase64().toString()

def get = new URL(artifactFolderURL).openConnection();
def getRC = get.getResponseCode();
def result

// check if http response is equal to 200
if(getRC.equals(200)) {
    result = get.getInputStream().getText()
}else{
    println("non-200 response code exiting..")
    System.exit(0)
}

// split result into array
def array = result.split("\n")

def versions = ""
// loop through result and fetch versions
for ( i=0; i<array.length; i++ ){
    // Loop through result get table rows with links, exclude items with word Parent Directory and maven-metada.xml
    if (array[i].contains("<td><a href") && ! array[i].contains("Parent Directory") && ! array[i].contains("maven-metadata.xml")){
        versions += array[i].split(">")[2].split("<")[0] + "\n"

    }
}
// println("Versions Available:\n" + versions);

// split versions into array
def sortedarray = versions.split("\n");

// i = position
for ( i=0; i<sortedarray.length; i++ ){     
    // assume position i is lowest
    smallest = i

    // search for the smallest value
    // start searching at index i
    for (j = i; j < sortedarray.length ; j ++ ){
        // check if equal value ng first digit(s)
        if (sortedarray[j].split("\\.")[0].toInteger() == sortedarray[smallest].split("\\.")[0].toInteger() ){
            // check if equal value ng second digit(s)
            if (sortedarray[j].split("\\.")[1].toInteger() == sortedarray[smallest].split("\\.")[1].toInteger()){
                // check if equal value ng third digit(s)
                if (sortedarray[j].split("\\.")[2].toInteger() != sortedarray[smallest].split("\\.")[2].toInteger()){
                    // if i is smaller than the smallest set i to smallest
                    if (sortedarray[j].split("\\.")[2].toInteger() < sortedarray[smallest].split("\\.")[2].toInteger() ){
                        smallest = j
                    }
                }
            }
            else{
                // if i is smaller than the smallest set i to smallest
                if (sortedarray[j].split("\\.")[1].toInteger() <= sortedarray[smallest].split("\\.")[1].toInteger() ){
                    smallest = j
                }
            }
        }
        // if digit 1 is not equal compare
        else{
            // if i is smaller than the smallest set i to smallest
            if (sortedarray[j].split("\\.")[0].toInteger() <= sortedarray[smallest].split("\\.")[0].toInteger() ){
                smallest = j
            }
        }
    }
    // swap values of smallest to index i
    // wag na mag swap kapag tama na yung pwesto
    if (smallest != i ){
        tmp = sortedarray[i]
        sortedarray[i] = sortedarray[smallest]
        sortedarray[smallest] = tmp
    }
    
}

println("Versions Available:\n" + sortedarray);

def deletelist = ""

for ( k=0; k<sortedarray.length - store; k++ ){
    deletelist += sortedarray[k] + "\n"
    query = new URL(artifactURL + sortedarray[k]).openConnection();
    response = query.getResponseCode();
    // check if http response is equal to 200
    if(response.equals(200)) {
        result = query.getInputStream().getText()
    }else{
        println("non-200 response code exiting..")
        System.exit(0)
    }
    resultArray = result.split("\n")
    resultID = ""
    for ( l=0; l<resultArray.length; l++){
        if(resultArray[l].contains("\"id\"")){
            // resultID += resultArray[l].split("\"")[3] + "\n"
            println(deleteAPI + resultArray[l].split("\"")[3])
            delete = new URL(deleteAPI + resultArray[l].split("\"")[3]).openConnection();
            delete.setRequestProperty( "Authorization", "Basic ${authString}")
            delete.setRequestMethod("DELETE")
            println(delete.getResponseCode())
            if(!delete.getResponseCode().equals(204)) {
                println("non-204 response code exiting..")
                System.exit(0)
            }
        }
    }
    println(sortedarray[k] + "ID: \n" + resultID)

}
for ( k=sortedarray.length - store; k<sortedarray.length ;k++ ){
    retainlist += sortedarray[k] + "\n"
}

println("Deleted the following: \n" + deletelist)
println("Retained the following: \n" + retainlist)







