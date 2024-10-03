# iTrackStream
#### This project will give you a perspective of how exploited you are when it comes to your data

What we're gonna do is this:
1. Make your iPhone listen to your vocal patterns, identify if you coughed.
2. Send that information to a httpserver 
3. Forward the data from httpserver to a kafka topic

##### The code is here in the repo, but you'll need to perform some steps on your iPhone first.

###### Create a Shortcut on your iPhone.
    1. Open Shortcuts App
    2. Tap on the + symbol at the top right corner, rename it your fancy, I named it "Send location to Kafka"
    3. Tap on the Add Action button
    4. Search and select "Get current Location" - this will retreive your latitude and longitude (more than that but that's all we need for this project)
    5. Tap on the Add Action button (or search for apps and actions inside the shortcut)
    6. Select the "Get contents of URL" 
    7. In the url variable, type: http://<ip_address_of_httpserver>:8080/send_gps - you'll need to lookup your ipaddress using cmd command "ipconfig" (Wireless LAN adapter Wi-Fi IPv4 address - if youre using a wifi router)
    8. Expand the action
    9. Change Method to POST
    10. Add new field, set key to latitude (just type it)
    11. When you tap on the value, it should open up your phone keyboard, above which you'll have the option to select "Get Location".
        Select "Get Location" and select Latitude 
    12. Add new field, set key to longitude (just type it)
    13. When you tap on the value, it should open up your phone keyboard, above which you'll have the option to select "Get Location".
        Select "Get Location" and select Longitude 
    14. Add new field, set key to timestamp (just type it)
    15. Tap on value, choose "Current Date" - should be beside "Get Location" and choose appropriate format, I used ISO
    16. Add new field, set key to details (just type it), set value to "I just coughed"
    17. Tap on done on the top right corner

###### Create an Automation on your iPhone.
    1. On your shortcuts app, switch to "Automation" tab on the bottom
    2. Tap on the + symbol
    3. Search and select "Sound Recognition"
    4. Under "When", sound -> Choose -> Cough -> Next
    5. Select the "Send location to Kafka" shortcut

That's it! Now, you just need your Kafka cluster and httpserver app running and listening on your laptop. 
You should receive your "cough coordinates" whenever you cough.

How is this gonna give you a perspective of how exploited you are when it comes to data?
Well, remember the time you gave a microphone access to a stupid game which it really didn't require? They've been listening...
Explore shortcuts and see all sorts of data that can be collected from your device., right from your contacts, to your photos, to your location, it's all just there.