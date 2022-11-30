#include "../include/connectionHandler.h"
#include <string>
#include <ctime>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port) :
        host_(host), port_(port), io_service_(), socket_(io_service_), commands() {
    initializeCommands();
}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception &e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp) {
            tmp += socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
//        std::cerr << "recv failed GET(Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if (error)
        throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLine(std::string &line) {
    return getFrameAscii(line, ';');
}


bool ConnectionHandler::sendLine(std::string &line) {
    return sendFrameAscii(line, ';');
}



//"translates" line from server
bool ConnectionHandler::getFrameAscii(std::string &frame, char delimiter) {
    char OpBytes; //the first two numbers
    char messageOpBytes; // if its an ack or error the next two numbers will of the message it was sent about
    char PmOrPost;
    std::string temp;
    try {
        getBytes(&OpBytes, 2); //getting the opcode of ack/error/notification
        short op = bytesToShort(&OpBytes);
        if (op == 11) {//error message
            frame.append("ERROR ");
        }
        else if (op == 10) {//ack message
            frame.append("ACK ");
        }
        else {//notification message;
            frame.append("NOTIFICATION ");

        }
        //if it is an ackorError than the next 2 bytes are the messageOp
        if (op == 11 || op == 10) {
            getBytes(&messageOpBytes, 2);
           // short arr[2]= {messageOpBytes[1],messageOpBytes[0]};
            short messageOp = bytesToShort(&messageOpBytes);
            temp.append(std::to_string(messageOp));
        }
        else {
            getBytes(&PmOrPost, 1);
            if(std::to_string(PmOrPost)=="1"){
                temp.append("Public");
            }
            else{
                temp.append("PM");
            }
        }
        frame.append(temp + " ");
        char curr;
        getBytes(&curr, 1);
        if (curr != ';') {
            do {
                if (curr == '\0') {
                    frame.append(1, ' ');
                } else if (curr != ';') {
                    frame.append(1, curr);
                }
                if (!(getBytes(&curr, 1))) {
                    return false;
                }
            } while (curr != ';');
        }
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}



//from the keyboard to the server
bool ConnectionHandler::sendFrameAscii(const std::string &frame, char delimiter) {
    //adding the op code
    std::string output;
    char bytesArr[2];
    size_t firstSpace = frame.find_first_of(' ');
    std::string nameOfAction = frame.substr(0, firstSpace); //position to start including, length;
    short op = commands.at(nameOfAction);
    shortToBytes(op, bytesArr);
    output.push_back(bytesArr[0]);
    output.push_back(bytesArr[1]);
    std::string temp;
    if (op == 5 || op == 12) {//POST or BLOCK --opString/0
        temp.append(frame.substr(firstSpace + 1, frame.length()));
        output.append(temp);
        output.push_back('\0');
        output.push_back(';');
    } else if (op == 8) { //STAT
        std::string list(frame.substr(firstSpace + 1, frame.length()));
        while (list.find_first_of(' ') != string::npos) {
            size_t s = list.find_first_of(' ');
            output += (list.substr(0, s));
            output.push_back('|');
            list = list.substr(s + 1, list.length());
        }
        output.append(list);
        output.push_back('|');
        output.push_back('\0');
        output.push_back(';');
    } else if (op == 4) {//FOLLOW
        output.push_back(frame.at(firstSpace + 1)); //saving 0 or 1
        temp.append(frame.substr(firstSpace + 3, frame.length()));
        output.append(temp);
        output.push_back('\0');
        output.push_back(';');
    } else if (op == 6) {//PM
        temp.append(frame.substr(firstSpace + 1, frame.length()));
        size_t s = temp.find_first_of(' ');
        output += (temp.substr(0, s));
        output.push_back('\0');
        temp = temp.substr(s + 1, temp.length());
        output.append(temp);
        output.push_back('\0');
        time_t now = time(0);
        tm *ltm = localtime(&now);
        std::string date =
                std::to_string(ltm->tm_mday) + "-" + std::to_string(1 + ltm->tm_mon) + "-" +
                std::to_string(1990 + ltm->tm_year) +  std::to_string(2 + ltm->tm_hour) + ":" +
                std::to_string(ltm->tm_min);
        output.append(date);
        output.push_back('\0');
        output.push_back(';');

    } else if (op == 1) {//REGISTER
        temp.append(frame.substr(firstSpace + 1, frame.length()));
        while (temp.find_first_of(' ') != string::npos) {
            size_t s = temp.find_first_of(' ');
            output += (temp.substr(0, s));
            output.push_back('\0');
            temp = temp.substr(s + 1, temp.length());
        }
        output.append(temp);
        output.push_back('\0');
        output.push_back(';');
    } else if (op == 2) {//LOGIN
        temp.append(frame.substr(firstSpace + 1, frame.length()));
        while (temp.find_first_of(' ') != string::npos) {
            size_t s = temp.find_first_of(' ');
            output += (temp.substr(0, s));
            output.push_back('\0');
            temp = temp.substr(s + 1, temp.length());
        }
        output.append(temp);
        output.push_back(';');
    }
    else if (op==3 || op==7){
        output.push_back(';');
    }
    size_t outputSize= output.length();
    bool result = sendBytes(output.c_str(), outputSize);
    if (!result)
        return false;
    return true;
}


// Close down the connection properly.
void ConnectionHandler::close() {
    try {
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

//----------our functions--------------------------------------------------------------------
void ConnectionHandler::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

short ConnectionHandler::bytesToShort(char *bytesArr) {
    short result = (short) ((bytesArr[0] & 0xff) << 8);
    result += (short) (bytesArr[1] & 0xff);
    return result;
}

void ConnectionHandler::initializeCommands() {
    commands["REGISTER"] = 1;
    commands["LOGIN"] = 2;
    commands["LOGOUT"] = 3;
    commands["FOLLOW"] = 4;
    commands["POST"] = 5;
    commands["PM"] = 6;
    commands["LOGSTAT"] = 7;
    commands["STAT"] = 8;
    commands["NOTIFICATION"] = 9;
    commands["ACK"] = 10;
    commands["ERROR"] = 11;
    commands["BLOCK"] = 12;
}