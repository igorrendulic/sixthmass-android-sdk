# SixthMass Android SDK

## Latest version [ ![Download](https://api.bintray.com/packages/sixthmass/SixthMassAndroidSDK/sixthmass-android-sdk/images/download.svg) ](https://bintray.com/sixthmass/SixthMassAndroidSDK/sixthmass-android-sdk/_latestVersion)

*May 06, 2017*

# Table of Contents

<!-- MArkdown TOC -->
- [Add SixthMass Analytics](#add-sixthmass-analytics)
    - [Installation](#installation)
    - [How to use](#how-to-use)
- [Want to contribute?](#want-to-contribute)
- [Changelog](#changelog)
- [License](#license)
<!-- MArkdown TOC -->

<a name="add-sixthmass-analytics"></a>
# Add SixthMass Analytics to your Android App

<a name="installation"></a>
## Installation

Add SixthMass to app level build.gradle (*app/build.gradle*) in the `dependencies` section

```java
compile 'com.sixthmass:sixthmass-android-sdk:0.0.4@aar'
```


<a name-="how-to-use"></a>
## How to use SixthMass tracking

All tracking methods have base/required parameters for tracking and optional custom properties. 

### Initialization

Initialize SixthMass SDK in your main activity (MainActivity.java)

Usually this is done in [onCreate method](https://developer.android.com/reference/android/app/Activity.html#onCreate(android.os.Bundle))
```java
SixthMass.init(this, "YOUR TOKEN HERE");
```

### Track Events

```java
SixthMass.track("My Event");
```

You can also add custom properties to each event. Properties are represented as a simple `Map<String,String>` object

```java
 Map<String,String> myProperties = new HashMap<String, String>() {{
	put("custom key 1", "custom value 1");
    put("custom key 2", "custom value 2");
    put("custom key 3", "custom value 3");
 }};

 SixthMass.track("My Event", myProperties);
```

### Update user profile

Profile object `SixthMassUserProfile` is automatically generated for you to use.

Get the object: 
```java
SixthMassUserProfile profile = SixthMass.getProfile();
```

Set values. All values are optional. You can partially update `SixtMassUserProfile`. The updates are accumulated.
```java
profile.setEmail("test@test.com");
profile.setRemoteUserId("1");
profile.setGender("male");
profile.setFirstName("Tester");
profile.setBusinessName("Business Inc.");
profile.setLastName("Tester");
profile.setBirthday(new Date());

// optional
 Map<String,String> myValues=  new HashMap<String, String>() {{
	put("custom key 1", "custom value 1");
    put("custom key 2", "custom value 2");
    put("custom key 3", "custom value 3");
}};

profile.setCustomValues(myValues);
```

Update profile:
```java
SixthMass.profileUpdate(profile);
```

### Track Registrations

*Recommended use*: When app has functionality for unregistered users that might become registered users

Use SixthMassUserProfile object

```java
SixthMassUserProfile profile = SixthMass.getProfile();

// update the object with data, e.g.:
profile.setEmail("test@test.com");

SixthMass.register(profile);
```

### Profile updates

You can gradually build up user profile as data becomes available

```java
SixthMassUserProfile profile = SixthMass.getProfile();
profile.setBirthday(new Date());
SixthMass.profileUpdate(profile);
```

### Track Purchase

Use SixthMassItem object to define the purchase item

```java
List<SixthMassItem> items = new ArrayList<>();
items.add(new SixthMassItem("D-TOY", "D-JOY Tri-Spinner Fidget", 2.90, 1));
items.add(new SixthMassItem("085715409164", "Banana Republic Classic, 4 .oz. Spray", 10.00, 5));
SixthMass.purchase(items);
```

<a name="want-to-contribute"></a>
# Want to Contribute?

The SixthMass library for Android is an open source project. You're welcome to contribute!

Steps to contribute: 
1. Fork this repository
2. Create local clone of your fork
3. Configure Git to sync your for with original SixthMass repository

These steps are described in detail [Here](https://help.github.com/articles/fork-a-repo/)

When you're done you can open a [pull request](https://help.github.com/articles/about-pull-requests/)


<a name="changelog"></a>
# Changelog

## 0.0.4 - 2017-13-05

- Activity Callbacks for resetting the session each time app is re-opened

<a name="License"></a>
# License

```
See LICENSE file for details. 
```


