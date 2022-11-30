package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.Messages.*;
import bgu.spl.net.api.bidi.BidiMessage;
import bgu.spl.net.api.bidi.Messages.Error;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BidiMessageEncoderDecoder implements MessageEncoderDecoder<BidiMessage> {
    //for opcode

    private short mOP;
    private byte[] bytes;
    private int num;
    private int progress;
    private int len;
    private byte[] bytesOP;
    //result management
    private boolean isDone;
    private BidiMessage result;
    //msg helpers
    private Boolean userTag;
    private byte[] postBytes;
    private int postLen;

    public BidiMessageEncoderDecoder() {
        bytesOP = new byte[2];
        progress = 0;
        isDone = false;
        bytes = new byte[1];
        len = 0;
        num = 0;
        mOP = 0;
        userTag = false;
        postBytes = new byte[1];
        postLen = 0;

    }

    @Override
    public BidiMessage decodeNextByte(byte nextByte) {
        try {
            if(progress<=2) {
                if (progress < 2) { //saving the first two for client op
                    bytesOP[progress] = nextByte;
                    progress++;
                }
                if (progress == 2) {
                    progress++;
                    mOP = bytesToShort(bytesOP);
                }
            }
            else if (nextByte == ';' && progress == 3) {
                if (mOP == 3) { //logout
                    result = new LogOut();
                    isDone = true;
                } else if (mOP == 7) { //logstat
                    result = new LogStat();
                    isDone = true;
                }
            }
            else if (progress >=3) {
                progress++;
                if (mOP == 5) { //post
                    decodeType1(nextByte);
                }
                if (mOP == 12) { //block
                    decodeType2(nextByte);
                }

                if (mOP == 1) { //register
                    decodeType3(nextByte);
                }
                if (mOP == 6) { //PM
                    decodeType4(nextByte);
                }
                if (mOP == 2) { //login
                    decodeType5(nextByte);
                }
                if (mOP == 8) { //stat
                    decodeType6(nextByte);
                }
                if (mOP == 4) { //follow
                    decodeType7(nextByte);
                }

            } else {
                throw new Exception("not a valid op");
            }

        } catch (Exception e) {
            result = new Error(mOP);
            isDone = true;
        }

        if (isDone) {
            progress = 0;
            bytesOP = new byte[2];
            len = 0;
            bytes = new byte[2];
            num = 0;
            isDone = false;
            userTag = false;
            postBytes = new byte[1];
            postLen = 0;
            mOP= 0;
            return result;
        }
        return null;
    }


    @Override
    public byte[] encode(BidiMessage message) {
        byte[] encodemOP ;
        byte[] encodeOp= shortToBytes(message.getOPCode());
        String res;
        if (message instanceof ACK) {
            encodemOP= shortToBytes(((ACK)message).getMsgOpCode());
            if (message instanceof FollowACK) {
                FollowACK followackMsg = (FollowACK) message;
                res = followackMsg.getUserName() + '\0';
            } else if (message instanceof LogStatNStatAck) {
                LogStatNStatAck logStatNStatAck = (LogStatNStatAck) message;
                res = logStatNStatAck.getAge() + " " + logStatNStatAck.getNumOfPosts() + " " + logStatNStatAck.getNumOfFollowers() + " " + logStatNStatAck.getNumOfFollowing();
            } else {
                res = "";
            }
            return helpEncode(res, encodeOp, encodemOP);
        } else if (message instanceof Error) {
            encodemOP= shortToBytes(((Error)message).getMOP());
            return helpEncode2(encodeOp, encodemOP);
        } else {//message instanceof Notification
            Notification notificationMsg = (Notification) message;
            res = notificationMsg.getPostingUser() + '\0' + notificationMsg.getContent() + '\0';

            //getting the return msg if exist one
            byte[] encodeRes = (res + ';').getBytes();
            int length = encodeRes.length + 3;
            byte[] notficationType = shortToBytes(notificationMsg.getType());
            byte[] ans = new byte[length];
            ans[0] = encodeOp[0];
            ans[1] = encodeOp[1];
            /////////////////
            ans[2] = notficationType[1];
            for (int i = 3; i < length; i++) { //putting the translate of the message to bytes int the answer
                ans[i] = encodeRes[i - 3];
            }
            return ans;
        }
    }

    //--------------------------------initializing the decoders--------------------------------------------------------
    //post
    private void decodeType1(byte b) {
        if (num == 1) {
            if (b != ';') {
                result = new Error(mOP);
                isDone = true;
            } else
                num++;
        }
        if (b == '\0' & !isDone)
            num++;
        if (userTag & !isDone) {
            if (b == ' ') {
                userTag = false;
                pushBytePost((byte) ' ');
            } else
                pushBytePost(b);
        }
        if (b == '@' & !isDone) {
            userTag = true;
        }
        if (num == 2 & !isDone) {
            String args = new String(bytes, 0, len, StandardCharsets.UTF_8);
//            String[] s = new String[]{args};
            String args2 = new String(postBytes, 0, postLen, StandardCharsets.UTF_8);
            String[] k = args2.split(" ");
            result = new Post(args, k);
            isDone = true;
        } else if (b != '\0' & !isDone)
            pushByte(b);
    }

    //block
    private void decodeType2(byte b) {
        if (num == 1) {
            if (b != ';') {
                result = new Error(mOP);
                isDone = true;
            } else
                num++;
        }
        if (b == '\0' & !isDone)
            num++;
        if (num == 2 & !isDone) {
            String args = new String(bytes, 0, len, StandardCharsets.UTF_8);
            result = new Block(args);
            isDone = true;
        } else if (b != '\0' & !isDone)
            pushByte(b);
    }


    //register
    private void decodeType3(byte b) {
        if (num == 3) {
            if (b != ';') {
                result = new Error(mOP);
                isDone = true;
            } else
                num++;
        }
        if (b == '\0' & !isDone) {
            num++;
            if (num < 3)
                pushByte((byte) ' ');
        }
        if (num == 4 & !isDone) {
            String args = new String(bytes, 0, len, StandardCharsets.UTF_8);
            String[] s = args.split(" ");
            result = new Register(s[0], s[1], s[2]);
            isDone = true;
        } else if (b != '\0' & !isDone)
            pushByte(b);
    }
    //PM
    private void decodeType4(byte b) {
        if (num == 3) {
            if (b != ';') {
                result = new Error(mOP);
                isDone = true;
            } else
                num++;
        }
        if (b == '\0' & !isDone) {
            num++;
            if (num < 3)
                pushByte((byte) '\0');
        }
        if (num == 4 & !isDone) {
            String args = new String(bytes, 0, len, StandardCharsets.UTF_8);
            String[] s = args.split("\0");
            result = new PM(s[0], s[1], s[2]);
            isDone = true;
        } else if (b != '\0' & !isDone)
            pushByte(b);
    }

    //login
    private void decodeType5(byte b) {
        if (num == 3) {
            if (b != ';') {
                result = new Error(mOP);
                isDone = true;
            } else
                num++;
        }
        if (num == 2 & !isDone) {
            num++;
            pushByte(b);
        } else if (b == '\0' & !isDone) {
            num++;
            pushByte((byte) ' ');
        } else if (num == 4 & !isDone) {
            String args = new String(bytes, 0, len, StandardCharsets.UTF_8);
            String[] s = args.split(" ");
            result = new LogIn(s[0], s[1], s[2]);
            isDone = true;

        } else if (b != '\0' & !isDone) {
            pushByte(b);
        }
    }

    //STAT
    private void decodeType6(byte b) {
        if (num == 1) {
            if (b != ';') {
                result = new Error(mOP);
                isDone = true;
            } else
                num++;
        }
        if (b == '\0' & !isDone)
            num++;
        if (num == 2 & !isDone) {
            String args = new String(bytes, 0, len, StandardCharsets.UTF_8);
            String[] s = args.split(" ");
            result = new Stat(s);
            isDone = true;
        } else if (b != '\0' & !isDone)
            pushByteUsers(b);
    }

    //action follow::to check in the forum if it will end with "\0" and if so its the correct decoder
    private void decodeType7(byte b) {
        if (num == 2) {
            if (b != ';') {
                result = new Error(mOP);
                isDone = true;
            } else
                num++;
        }
        if ((b == (byte) '1' || b == (byte) '0') & num == 0) {
            pushByte(b);
            pushByte((byte) ' ');
            num++;
        } else if (b == '\0' & !isDone)
            num++;
        else if (!isDone & num == 3) {
            String args = new String(bytes, 0, len, StandardCharsets.UTF_8);
            String[] s = args.split(" ");
            result = new Follow_UnFollow(s[0], s[1]);
            isDone = true;
        } else if (b != '\0' & !isDone) {
            pushByte(b);
        }
    }


// ---------------------------------------------helping functions-----------------------------------------------------

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    //for STAT
    private void pushByteUsers(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        if (nextByte == ((byte) '|')) {
            bytes[len++] = ' ';
        } else {
            bytes[len++] = nextByte;
        }
    }

    private void pushBytePost(byte nextByte) {
        if (postLen >= postBytes.length) {
            postBytes = Arrays.copyOf(postBytes, postLen * 2);
        }
        postBytes[postLen++] = nextByte;
    }


    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    public byte[] helpEncode(String res, byte[] encodeOp, byte[] encodemOP) {
        byte[] encodeRes = (res + ';').getBytes();
        int length = encodeRes.length + 4;

        byte[] ans = new byte[length];
        ans[0] = encodeOp[0];
        ans[1] = encodeOp[1];
        ans[2] = encodemOP[0];
        ans[3] = encodemOP[1]; //placing the return op and og msg op in first
        for (int i = 4; i < length; i++) { //putting the translate of the message to bytes int the answer
            ans[i] = encodeRes[i - 4];
        }
        return ans;
    }

    public byte[] helpEncode2(byte[] encodeOp, byte[] encodemOP) {
        byte[] ans = new byte[5];
        byte[] encodeRes = ("" + ';').getBytes();
        ans[0] = encodeOp[0];
        ans[1] = encodeOp[1];
        ans[2] = encodemOP[0];
        ans[3] = encodemOP[1];
        ans[4] = encodeRes[0];
        return ans;
    }

}