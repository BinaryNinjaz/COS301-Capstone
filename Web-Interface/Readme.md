## Setting up Firebase

When you run the 'firebase login' command you'll be prompted to login to firebase. Use the binary ninjaz firebase account.

```
npm install --global -firebase-tools
cd Web-Interface
firebase login
firbase serve
```

This will setup a localhost on port 5000.

In your browser go to "localhost:5000/Login.html"

login using an account. If you dont have one set up one in Firebase console. Or use the register page.


**Remember to use the CDNs at deployment. And if you are connected to the internet, it is most probably better to be using them over the locals when testing.**

-Kevin
