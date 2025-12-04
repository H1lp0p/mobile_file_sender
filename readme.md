# Clinet for [broadcast data host](https://github.com/H1lp0p/broadcast_data_host)
- transfers data via local network
- uses password `pass` (can be edited)
- send's files every 15 minutes if allowed (15 minutes between uploads)

# How to run
- install .apk from [release](https://github.com/H1lp0p/mobile_file_sender/releases) (or build your own)
- allow every permisssion in settings
- run app

# How to set
- enter password and go to second creen
- sellect folder with "SELECT" button
- enter car license plate number (DO NOT FORGET TO UPDATE with "✔" button). Current car license number will shown near title
- press "START monitoring"
- all working status (with errors) will shown in push notifications

> App will work in background even after device restart

- to stop background server press "STOP monitoring"

# Important to know
- You can change password in [PasswordScreenViewModel.kt](app/src/main/java/com/broadcastdata/main/screens/viewmodels/PasswordScreenViewModel.kt)

```kotlin
    companion object{
        const val PASSWORD = "pass"
    }
```

- worker will ignore nested folders and will send only files from selected folder

# todo's for future maintenance
- [ ] do something with ssl certificates on host side (now it works with [network_security_config.xml](app/src/main/res/xml/network_security_config.xml) which is not very good)
- [ ] move secret key and password to build settings
- [ ] change simple sha encoded message to more secure verification pipeline
- [ ] add upload optimization to sync which files to upload
