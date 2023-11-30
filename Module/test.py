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
from datetime import datetime
from pytz import timezone
import asyncio
import firebase_admin
from firebase_admin import db
from firebase_admin import firestore 



credentials = firebase_admin.credentials.Certificate('ledcontrol-7c9d9-firebase-adminsdk-om517-3f07b22872.json')
default_app = firebase_admin.initialize_app(credentials,{'databaseURL': 'https://ledcontrol-7c9d9-default-rtdb.firebaseio.com'})
firestore_db = firestore.client()


ROOM = ""
SERIAL = ""
CURRENT_TIMESTAMP = 0
DAY_HASH = {0: "Monday", 1: "Tuesday", 2: "Wednesday", 3: "Thursday", 4: "Friday", 5:"Saturday", 6:"Sunday"}

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
    ROOM = ref.get()["room"]
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
 
def getEmail(android_ID):
  global SERIAL
  users = firestore_db.collection("USERS")
  query = users.where("android_id", "==", android_ID).stream()
  for a in query:
    # print(a.to_dict())
    print(a.id)
    return a.id
  
def getCurrentClass(current_day_of_week, current_hour, current_minute):
  global ROOM
  courses = firestore_db.collection("COURSES")
  query = courses.where("ROOM_NUMBER", "==", ROOM).stream()
  for a in query:
    dicto = a.to_dict()
    END_HOUR = dicto["END_HOUR"]
    END_MINUTE = dicto["END_MIN"]
    START_HOUR = dicto["START_HOUR"]
    START_MINUTE = dicto["START_MIN"]
    start_sec = (START_HOUR * 60 * 60) + (START_MINUTE * 60)- 60*15
    end_sec = (END_HOUR * 60 * 60) + (END_MINUTE * 60)
    current_sec = (current_hour * 60 * 60) + (current_minute * 60) 
    if current_day_of_week in dicto["DAYS"]:
      if (current_sec >= start_sec and current_sec <= end_sec):
        return a.id
    return "null"
  
def checkIfNeedForceRemoveStudents(current_time_sec):
     #checks if there's a need to remove students that have not signed out of class
  current_day_of_week = DAY_HASH[datetime.now(timezone('EST')).weekday()]
  # print(current_day_of_week)
  global ROOM
  courses = firestore_db.collection("COURSES")
  query = courses.where("ROOM_NUMBER", "==", ROOM).stream()
  # query = courses.stream()
  classNames = []
  classEndTimes = []
  for a in query:
    dicto = a.to_dict()
    # print(dicto)
    END_HOUR = dicto["END_HOUR"]
    END_MINUTE = dicto["END_MIN"]
    START_HOUR = dicto["START_HOUR"]
    START_MINUTE = dicto["START_MIN"]
	  
    DAYS = dicto["DAYS"]
    end_sec = (END_HOUR * 60 * 60) + (END_MINUTE * 60)
    start_sec = (START_HOUR * 60 * 60) + (START_MINUTE * 60)
    print("Class is: ", a.id)
    print("current time is: ", current_time_sec)
    print("end sec: ", end_sec)
    print("start sec: ", start_sec)


    # print(DAYS)
    if (current_day_of_week in DAYS):
      #  print("Found class")
      #  print("END HOUR: ", END_HOUR)
      #  print("END MINUTE: ", END_MINUTE)

      if ( (current_time_sec >= (end_sec+ 60*15)) and (current_time_sec > start_sec) ):
          # print("Current time: ", current_time_sec)
          # print("End time: ", end_sec)
          # print("ID: ", a.id)
          
          classNames.append(a.id)
          classEndTimes.append(end_sec)
  
  max_end_sec = 99999999999
  most_recent_class = "null"
  for i in range(0, len(classEndTimes)):
      if (classEndTimes[i] < max_end_sec):
          max_end_sec = classEndTimes[i]
          most_recent_class = classNames[i]
  print(most_recent_class)
  return most_recent_class

def forceRemoveStudents(course):
  global ROOM
  path = '/PRESENCE/%s' %(ROOM)
  ref = db.reference(path)
  dicto = ref.get()
  students_to_remove = []
  hours = []
  minutes = []
  for student in dicto:
    #  print(student)
     if (dicto[student]['present']):
        # print("Call to remove student")
        students_to_remove.append(student)
        hours.append(int(dicto[student]['time_in'].split(",")[3].split(':')[0]))
        minutes.append(int(dicto[student]['time_in'].split(",")[3].split(':')[1]))
  for student in students_to_remove:
    path = '/PRESENCE/%s/%s' %(ROOM, student)
    ref = db.reference(path)
    ref.update({'present': False})

    # pushFireStoreData(getCurrentClass(current_day_of_week, int(datetime.now(timezone('EST')).strftime("%H")), int(datetime.now(timezone('EST')).strftime("%M"))), datetime.now(timezone('EST')).strftime("%d_%m_%Y"), student)
    print("Removed student: ", student)
    emails = getEmailStudentRemoved(student)
    email = corroborateEmailWithTime(emails, hours[students_to_remove.index(student)], minutes[students_to_remove.index(student)], course)
    updateForceRemove(email, course)
        
def getEmailStudentRemoved(student_id):
  users = firestore_db.collection("USERS")
  query = users.where("android_id", "==", student_id).stream()
  emails = []
  for a in query:
    # print(a.to_dict())
    print(a.id)
    emails.append(a.id)
  return emails

def corroborateEmailWithTime(emails, hour, minute, course):
  courses = firestore_db.collection("COURSES")
  docref = courses.document(course)
  dicto = docref.get().to_dict()
  current_day = datetime.now(timezone('EST')).strftime("%d")
  current_month = datetime.now(timezone('EST')).strftime("%m")
  current_year = datetime.now(timezone('EST')).strftime("%Y")

  for email in emails:
    encoded_email = ''
    for character in email:
        encoded_email = encoded_email + str(ord(character)) + "_"
    try:
      arrival_hour =  dicto['PRESENT'][current_day + "_" + current_month + "_" + current_year][encoded_email]["arrival_hour"]
      arrival_minute =  dicto['PRESENT'][current_day + "_" + current_month + "_" + current_year][encoded_email]["arrival_minute"]
      arrival_seconds = arrival_hour * 60 * 60 + arrival_minute * 60
      class_start_seconds = hour * 60 * 60 + minute * 60
      if (class_start_seconds - 15*60 <= arrival_seconds ):
        print(email)
        return email
      #get information in path
    except:
      print("Student did not arrive: ", email)
      
       

   
  
def updateForceRemove(studentEmail, course):
  current_day = datetime.now(timezone('EST')).strftime("%d")
  current_month = datetime.now(timezone('EST')).strftime("%m")
  current_year = datetime.now(timezone('EST')).strftime("%Y")
  courses = firestore_db.collection("COURSES")
  utf_8_email = studentEmail.encode('utf-8')
  encodedEmail = ''
  for character in studentEmail:
     encodedEmail = encodedEmail + str(ord(character)) + "_"
    #  print(str(ord(character)))
     
  # print(encodedEmail)
    # path = "PRESENT" + "." + current_day + "_" + current_month + "_" + current_year + "." + str(studentEmail.replace(".", "_")) + "." + "forced_remove"
  path = "PRESENT" + "." + current_day + "_" + current_month + "_" + current_year + "." + encodedEmail + "." + "forced_remove"
  
  docref = courses.document(course)
  docref.update({path: True})

def pushFireStoreData(course, date_string, email):
  ref = firestore_db.collection("COURSES").document(course)
  path = "PRESENCE" + "." + date_string 
  ref.update({path: email})

def authenticate_class(dictionary):
  if (dictionary['id']== "null"):
    print("Found null, will not update database")
    return
  arr = dictionary['id'].split("_")
  print(arr)
  try:
      action = arr[0]
      uid = arr[1]
  except:
      print("Not enough arguments")
      return
  try:
      extra = arr[2]
  except:
      extra = None
  global ROOM
  global CURRENT_TIMESTAMP
  #get current timetamp for EST timezone
  tz = timezone('EST')
  CURRENT_TIMESTAMP = datetime.now(tz) 
  #get current day of week
  day_of_week = CURRENT_TIMESTAMP.strftime("%A")
  current_hour = CURRENT_TIMESTAMP.strftime("%H")
  current_minute = CURRENT_TIMESTAMP.strftime("%M")
  current_day_of_month = CURRENT_TIMESTAMP.strftime("%d")
  current_month = CURRENT_TIMESTAMP.strftime("%m")
  current_year = CURRENT_TIMESTAMP.strftime("%Y")

  date_string = current_day_of_month + "_" + current_month + "_" + current_year

  # course = getCurrentClass(day_of_week, int(current_hour), int(current_minute))


  if action == "CI":
      print("CI")
      email = getEmail(uid)
      path = '/PRESENCE/%s/%s' %(ROOM, uid)
      present_status = database_present_state(path)
      #covert timestamp to string containing day, month, year, hour, minute, second, in 24 hour format
      database_push_data(path, {'present': True, 'time_in' :  CURRENT_TIMESTAMP.strftime("%b,%d,%Y,%H:%M:%S") })
      return
  elif action == "CO":
      print("CO")
      email = getEmail(uid)
      path = '/PRESENCE/%s/%s' %(ROOM, uid)
      present_status = database_present_state(path)
      database_push_data(path, {'present': False, 'time_out' : CURRENT_TIMESTAMP.strftime("%b,%d,%Y,%H:%M:%S")})
      #getCurrentClass(current_day_of_week, current_hour, current_minute)
      return
  elif action == "UR":
      print("UR")
      if extra == None:
        print("Error: No extra when it is required")
      else:
        setClassroomNumber(extra)
      return
  else :
      print("Error: Invalid action")
      return
  return
  
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
  start_time_hour = int(datetime.now(timezone('EST')).strftime("%H"))
  start_time_minute = int(datetime.now(timezone('EST')).strftime("%M"))
  start_time_sec = int(datetime.now(timezone('EST')).strftime("%S"))

  sec_ref = start_time_hour * 60 * 60 + start_time_minute * 60 + start_time_sec
  
  while True:
    
    dictionary = loop()
    start_time_hour = int(datetime.now(timezone('EST')).strftime("%H"))
    start_time_minute = int(datetime.now(timezone('EST')).strftime("%M"))
    start_time_sec = int(datetime.now(timezone('EST')).strftime("%S"))

    current_time_sec = start_time_hour * 60 * 60 + start_time_minute * 60 + start_time_sec
    # print("Current time: ", current_time_sec)
    # print("Start time: ", start_time_sec)
    # time.sleep(1)
    # print(current_time_sec - start_time_sec)
    if (current_time_sec - sec_ref >= 60):
      print("Calling force remove students")
      start_time_hour = int(datetime.now(timezone('EST')).strftime("%H"))
      start_time_minute = int(datetime.now(timezone('EST')).strftime("%M"))
      start_time_sec = int(datetime.now(timezone('EST')).strftime("%S"))

      sec_ref = start_time_hour * 60 * 60 + start_time_minute * 60 + start_time_sec
      courses = checkIfNeedForceRemoveStudents(current_time_sec)
      if (courses != "null"):
        try:
          print("Force removing students")
          forceRemoveStudents(courses)
        except (Exception) as error:
          print("Error in force remove students")
          # error.printStackTrace()



    if (dictionary != None):
      authenticate_class(dictionary)

main()

# SERIAL = getSerial()
# getClassroomNumber()
# getEmail("12345")
# getCurrentClass("Monday", 16, 50)
