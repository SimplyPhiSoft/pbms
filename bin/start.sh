
#!/usr/bin/env bash


function run_pbms {
    
    jar_file=`ls -tr target/bookmark-service-*-with-dependencies.jar |tail -1`

    if [ -z "$jar_file" ] 
    then
        echo "FATAL Error: Jar file not present;";
    fi;
    echo "Starting the server by running the jar : $jar_file"
    java -jar $jar_file
}

function build_pbms {
    #checking if maven is present in $PATH
    mvn --version > /dev/null
    if [ $? -ne 0 ] 
    then
        echo " Error: Maven is not present in your PATH; please install maven or add it to PATH before running me"
        exit 1;
    fi

    if [ ! -f `pwd`"/pom.xml" ]
    then
        echo "Error: This directory is not root of maven project; Please run me from directory where you cloned personal Bookmarklet Service;"
        exit 2;
    fi

    echo "Compile, Test and then Packaging the jar; This will take some time...."
    mvn clean package
    if [ $? -ne 0 ] 
    then
        echo "Error: Failed to build "
        exit 127;
    fi;
}

jar_file=`ls -tr target/bookmark-service-*-with-dependencies.jar |tail -1`

if [ -z "$jar_file" ] 
then
   build_pbms
fi;

run_pbms











    



