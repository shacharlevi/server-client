#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <iostream>
#include <thread>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int  main(int argc, char *argv[]) {
        if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }

    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
	//From here we will see the rest of the ehco client implementation:
    //reads from keyboard
    int logOut= 0;
    //thread ([captures- list of outside variables accessible from withing the lambda expression]
    std::thread th([&connectionHandler,&logOut](){
        bool terminated= false;
        while(!terminated){
            const short bufsize = 1024;
            char buf[bufsize];
            //reads from keyboard
            std::cin.getline(buf, bufsize); //gets line and puts it in buf
            std::string line(buf);
            if(!connectionHandler.sendLine(line)) {
                break;
            }
            int fSpace = line.find_first_of(' ');
            std::string type = line.substr(0,fSpace);
            if (line=="LOGOUT"){
                while (logOut == 0);
                if(logOut == 2)
                    terminated = true;
                logOut = 0;
            }
        }
    });
    th.detach();
    while(true) {
        std::string answer;
        if (!connectionHandler.getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            logOut=2;
            break;
        }
        std::cout<<answer<<std::endl;
        int space = answer.find_first_of(' ');
        std::string msgType = answer.substr(0,space);
        if(msgType=="ACK") {//answer= ACK op optional;
            std::string leftOver= answer.substr(space+1,answer.length());
            //getting the op
            int secondOp = leftOver.find_first_of(' ');
            if(secondOp==-1) { //only op code left
                if(leftOver=="3"){ //logout
                    std::cout << "Exiting...\n" << std::endl;
                    logOut=2;
                    break;
                }
            }
        }

    }
    return 0;
}
