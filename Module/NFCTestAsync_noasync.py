#Code to send a string from the phone (Android app) to the NFC receiver. Currently works, and hopefully is all we need.
#Best yet. We removed all async functions. Communicates with database and changes the status within a second. 
#NFC consecutive worse case reads are slightly slow

import time
import binascii
import json 

from pn532pi import Pn532
from pn532pi import Pn532Hsu
from pn532pi import Pn532I2c
from pn532pi import Pn532Spi
import asyncio
import firebase_admin
from firebase_admin import db

credentials = firebase_admin.credentials.Certificate('ledcontrol-7c9d9-firebase-adminsdk-om517-3f07b22872.json')
default_app = firebase_admin.initialize_app(credentials,{'databaseURL': 'https://ledcontrol-7c9d9-default-rtdb.firebaseio.com'})

ROOM = ""
SERIAL = ""

# Set the desired interface to True
SPI = False
I2C = True
HSU = False

if SPI:
    PN532_SPI = Pn532Spi(Pn532Spi.SS0_GPIO8)
    nfc = Pn532(PN532_SPI)
# When the number after #elif set as 1, it will be switch to HSU Mode
elif HSU:
    PN532_HSU = Pn532Hsu(Pn532Hsu.RPI_MINI_UART)
    nfc = Pn532(PN532_HSU)

# When the number after #if & #elif set as 0, it will be switch to I2C Mode
elif I2C:
    PN532_I2C = Pn532I2c(1)
    nfc = Pn532(PN532_I2C)



import subprocess
def getSerial():
    cpuserial = "null"

    try:
        f = open('/proc/cpuinfo', 'r')
        for line in f:
            if line[0:6]=='Serial':
                cpuserial = line[10:26]
        f.close()
    except:
        cpuserial = "null"

    return cpuserial

def getClassroomNumber():
  global SERIAL
  path = '/PI_MODULES/%s' %(SERIAL)
  ref = db.reference(path)
  global ROOM
  ROOM = "null"
  try:
    ROOM = ref.get()["Room"]
    print("Room found: ", ROOM)
  except:
    print("this serial number does not have a room")

def setClassroomNumber(roomNumber):
  global SERIAL
  path = '/PI_MODULES/%s' %(SERIAL)
  ref = db.reference(path)
  dictionary = {'room': roomNumber}
  ref.set(dictionary)
  global ROOM
  ROOM = roomNumber
 

  

def authenticate_class(dictionary):
  print (dictionary)
  print()
  if (dictionary['id']== "null"):
    print("Found null, will not update database")
    return
  elif (dictionary['id'] == "BRUH"):
    setClassroomNumber(dictionary['id'])
  else:
    global ROOM
    path = '/PRESENCE/%s/%s' %(ROOM, dictionary['id'])
    present_status = database_present_state(path)

    if (present_status == True):
      dict_to_push = {"present" : False}
    elif (present_status == False): 
      dict_to_push= {"present" : True}
    else:
      dict_to_push={"present" : True}
    
    database_push_data(path, dict_to_push)

def database_present_state(path):
  ref = db.reference(path)
  try:
    return ref.get()["present"]
  except:
    return "Non Existant in database"
    

  # return ref.get()["present"]

def database_push_data(path, dictionary):
  ref = db.reference(path)
  ref.set(dictionary)


def setup():
  print("Checking for serial number:")
  global SERIAL
  SERIAL = getSerial()
  if (SERIAL == "null"):
	  print("failed to find a serial number")
	
  getClassroomNumber()
	
	
  print("-------Peer to Peer HCE--------")
	
  nfc.begin()

  versiondata = nfc.getFirmwareVersion()
  if not versiondata:
    print("Didn't find PN53x board")
    raise RuntimeError("Didn't find PN53x board")  # halt

  # Got ok data, print it out!
  print("Found chip PN5 {:#x} Firmware ver. {:d}.{:d}".format((versiondata >> 24) & 0xFF, (versiondata >> 16) & 0xFF,
                                                             (versiondata >> 8) & 0xFF))

  # Set the max number of retry attempts to read from a card
  # This prevents us from waiting forever for a card, which is
  # the default behaviour of the PN532.
  #nfc.setPassiveActivationRetries(0xFF)

  # configure board to read RFID tags
  nfc.SAMConfig()

def loop():
  print("Waiting for an ISO14443A card")

  # set shield to inListPassiveTarget
  success = nfc.inListPassiveTarget()

  if (success):

    print("Found something!")

    selectApdu = bytearray([0x00,                                     # CLA 
                            0xA4,                                     # INS 
                            0x04,                                     # P1  
                            0x00,                                     # P2  
                            0x07,                                     # Length of AID  
                            0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, # AID defined on Android App 
                            0x00 # Le
                            ])

    success, response = nfc.inDataExchange(selectApdu)

    if (success):

      print("responseLength: {:d}", len(response))
      output_string = ""
      for byt in response:
        output_string = output_string + (chr(byt))
      print(output_string)
      print(binascii.hexlify(response))
      return{'id': output_string}
    else:
      print("Failed sending SELECT AID")
  else:
    print("Didn't find anything!")

  time.sleep(0.5)
  return None


def setupNFC():
  nfc.begin()

  versiondata = nfc.getFirmwareVersion()
  if not versiondata:
    print("Didn't find PN53x board")
    raise RuntimeError("Didn't find PN53x board")  # halt

  # Got ok data, print it out!
  print("Found chip PN5 {:#x} Firmware ver. {:d}.{:d}".format((versiondata >> 24) & 0xFF, (versiondata >> 16) & 0xFF,
                                                              (versiondata >> 8) & 0xFF))

  # configure board to read RFID tags
  nfc.SAMConfig()

def main():
  setup()
  while True:
  
    dictionary = loop()
      
    if (dictionary != None):
      authenticate_class(dictionary)

main()
