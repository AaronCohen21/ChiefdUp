import sys
import telnetlib

#get the local device port via command line arguments
localDevicePort = str(sys.argv[1])

#The loop that runs when listening to logcat
def dump_logcat(connection):
    while True:
        data = connection.read(1024)
        if not data:
            tn.close()
            print("Device disconnected")
            break
        message = str(data.decode('utf-8')).partition("I GAMEPIN : ")
        #if the logged message contains a game pin at the end of the tuple
        if len(message[2]) == 6:
            tn.write(("redir add tcp:" + message[2][0:5] + ":" + message[2][0:5] + "\n").encode('ascii'))
            print("     Redirected tcp: " + message[2][0:5])

    connection.close()

#connect telnet
tn = telnetlib.Telnet("localhost", localDevicePort)
tn.read_until("OK".encode('ascii'))
#connect to the client via ADB
from ppadb.client import Client as AdbClient
client = AdbClient(host="127.0.0.1", port=5037)
device = client.device("emulator-" + localDevicePort)
print("Connected to device localhost:" + localDevicePort)
#clear the logs and listen for new game pins that have been registered
device.shell("logcat -c all")
device.shell("logcat GAMEPIN:I *:S", handler=dump_logcat)