# WheelPicker

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/studio.clapp/wheelpicker/badge.svg)](https://maven-badges.herokuapp.com/maven-central/studio.clapp/wheelpicker/)

## Including in your project

### Gradle

Add below codes to your **project** `build.gradle` file.

````gradle
buildscript {
    repositories {
        mavenCentral()
    }
}
````

And add a dependency code to your **module**'s `build.gradle` file.

````gradle
dependencies {
    implementation 'studio.clapp:wheelpicker:1.0.1'
}
````

### Maven

````maven
<dependency>
  <groupId>studio.clapp</groupId>
  <artifactId>wheelpicker</artifactId>
  <version>1.0.0</version>
  <type>aar</type>
</dependency>
````

## Sample Usage

````xml

<studio.clapp.wheelpicker.WheelPicker
    android:id="@+id/wheel_picker"
    android:layout_width="300dp"
    android:layout_height="250dp"
    android:layout_marginTop="64dp"
    app:align="CENTER"
    app:selectedTextScale="1.2"
    app:textSize="42sp"
    app:wheelItemCount="5" />
````

## WheelPicker Attributes

|Attr|Type|Description|
|---|:---|:---:|
|selectedTextColor|color|Text color of selected item|
|selectedTextScale|float|Text scale of selected item|
|textColor|color|Text color of unselected item|
|textSize|dimension|Text size|
|wheelItemCount|integer|How much items will be visible to user|
|align|enum|Text align [LEFT, CENTER, RIGHT]|
|textSize|dimension|Text size|
|fadingEdgeEnabled|boolean|Whether text will be faded at top and bottom sides or not|
|max|integer|Maximum visible item index|
|min|integer|Minimum visible item index|

## TimePickerDialog

Simple customizable dialog to pick time with help of wheel pickers.

### Sample Usage

#### From activity:

````kotlin
TimePickerDialog.Builder(this).build().show()
````

#### From fragment:

````kotlin
TimePickerDialog.Builder(requireContext()).build().show()
````

### To get dialog result

````kotlin
TimePickerDialog.Builder(this).setOnPickedListener { hours, minutes -> println("$hours $minutes") }
    .build()
    .show()
````

### To set initial time

````kotlin
TimePickerDialog.Builder(this).setSelectedTime("23", "55")
    .build()
    .show()
````

# License

WheelPicker is distributed under the MIT license.
See [LICENSE](https://github.com/Clapp-Studio/WheelPicker/blob/master/LICENSE) for details.
