
IPCamp Competition - Signal Simulator
===

A solution by [Szabó Gergely](https://github.com/Gerviba).

## Build, test, setup

- Show in the IDE: You need to have [Lombok](https://projectlombok.org/setup/eclipse) installed.
- Requirements: Java 8+, Maven
- Build: `$ mvn clean install`
- Test: `$ mvn test`
- Setup: The compiled jar file is executable. (Thanks to spring-boot) It can also be used as a web archive (.war file). 

`$ java -Xmx<memory>M -Dspring.profiles.active=<mode> -jar simulator-<version>.jar`

## Configuration

![WebUI Config](https://raw.githubusercontent.com/Gerviba/signal-simulator/master/screenshots/1.png)

### Config files

Custom config files are available for every profiles.

- `src/main/resources/application.properties` : Global settings
- `src/main/resources/application-<mode>.properties` : Settings for the specified mode

Any configuration property can be set using the `-D<property.name>=` command line argument.

All of the custom properties are documented. See `src/main/resources/META-INF/additional-spring-configuration-metadata.json` file for more.

**NOTE**: Slave nodes must be configured with the same **input source**, **protocol JSON** and **transporter** properties as the master to work properly.

### Protocol JSON

```
- (Optional) Array of VehicleTypes
	- VehicleType: class
		- vehicleType: String
		- frames: array of Frame
			- name: String
			- frameID: number
			- period: number
			- signals: array of Signal
				- name: String
				- position: number
				- length: number
```

- `VehicleType`, `Frame` and `Signal` is located in the `hu.gerviba.simulator.model` package.
- Numbers are integers (max 8 byte)
- Examples: `test/configs/` folder
- Use `simulator.config-file` property in the properties file or the `-Dsimulator.config-file=` command line argument to specify the protocol json.

## Profiles (Modes)

|Profile name|Description|
|-----------:|-----------|
|`standalone`|Simple behaviour.|
|`master`|Accepts and discovers slaves. Manages the slave nodes.|
|`slave`|Works as a slave node.|

#### How to set profiles?

Edit the `application.properties` file or use the `-Dspring.profiles.active=` command line argument.

## Input Sources

### RandomFlowInputGenerator

> Class name: `hu.gerviba.simulator.input.RandomFlowInputGenerator`

Generates smooth signal data.

The algorithm is as follows:

- Generate random starting values for every signal per vehicle instance.
- Sets the delta values to 0 for every signal per vehicle instance.
- Every time the scheduler runs the update task: The delta value will be randomly modified and added (or subtracted if it's negative) to the value.

Inspired by 2nd derivatives.

### RandomInputGenerator

It generates random data using a simple random generator and masks the generated numbers to match the signal length.

### LogBasedInputSource

> Class name: `hu.gerviba.simulator.input.LogBasedInputSource`

This is an example of how can it be used for sending recorded data.

## Transporters

### HTTP Transporter

> Class name: `hu.gerviba.simulator.transport.HTTPTransporter`

Settings are the following:

```properties
simulator.http.host=127.0.0.1
simulator.http.port=80
simulator.http.method=POST
simulator.http.action=cloud/
simulator.http.basic-enabled=false
simulator.http.basic-username=username
simulator.http.basic-password=password
```

Both HTTP and HTTPS are supported.

I wrote a simple PHP script to test the class:

```PHP
<?php //name: /var/www/html/cloud/index.php
	$data = file_get_contents("php://input");
	foreach (str_split($data) as $part)
		echo ord($part) . " ";
	echo "\n";
```

### MQTT Transporter

> Class name: `hu.gerviba.simulator.transport.MQTTTransporter`

Settings are the following:

```properties
simulator.mqtt.host=127.0.0.1
simulator.mqtt.port=1883
simulator.mqtt.keep-alive=60
simulator.mqtt.id=simulator
simulator.mqtt.topic=simulator
simulator.mqtt.username=testu
simulator.mqtt.password=testp
```

- If you leave the username empty, authentication will be disabled.
- Currently only QoS=0 supported.
- I found [mosquitto](https://mosquitto.org/) very useful for testing.
- [MQTT documentation](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/csd02/mqtt-v3.1.1-csd02.html)

### Debugging

There are three debugging transporter:

- DebugTransporter : Prints packets to standard out
- MockTransporter : Can be used for testing
- NullTransporter : Will ignore any data

## RestAPI

### Master

To enable: use profile `master`

|Method|Path|Arguments|Description|
|:----:|----|---------|-----------|
| GET  |`/api/slaves`| *none* |Returns the list of all slaves|

### Master and Standalone

|Method|Path|Arguments|Description|
|:----:|----|---------|-----------|
| GET  |`/api/status/server`| *none* |Returns server status. Used in WebUI dashboard.|
| GET  |`/api/inputgenerator/start`| *none* |Starts the input generator.|
| GET  |`/api/inputgenerator/stop`| *none* |Stops the input generator.|
| GET  |`/api/cluster/start`| *none* |Sends start signals to slaves.|
| GET  |`/api/cluster/stop`| *none* |Sends stop signals to slaves.|

### Slave

To enable: use profile `slave`

|Method|Path|Arguments|Description|
|:----:|----|---------|-----------|
| GET  |`/api/slave/status`|`apiKey:String`|Results statistic from this node|
| POST |`/api/slave/start`|`command:CommandDao`|Send command to node|
| POST |`/api/slave/stop`| *none* |Send stop signal|

## WebUI

To enable: use profile `standalone` or `master`.<br>
**NOTE**: Config property `simulator.enable-webui` must be true.

#### Dependencies

- JavaScript, JQuery : Client side
- Google [fonts](https://fonts.google.com/), Google [material icons](https://material.io/tools/icons/?style=baseline) : Client side
- Spring MVC, Thymeleaf : Server side

#### Endpoints

|Method|Path|Description|
|:----:|----|-----------|
| GET  |`/`|Redirection to /webui|
| GET  |`/webui/`|WebUI dashboard, realtime statistics|
| GET  |`/webui/controls`|Input generator (ON/OFF)|
| GET  |`/webui/settings`|Vehicle count|
| GET  |`/webui/cluster`|List of slave nodes|

#### Form actions

|Method|Path|Arguments|Description|
|:----:|----|---------|-----------|
| POST |`/webui/range`|`name:String`, `count:Long`|Set the count of the selected vehicle|
| POST |`/webui/controls/start`| *none* |InputSource instance - `start()` method.|
| POST |`/webui/controls/stop`| *none* |InputSource instance - `stop()` method.|
| POST |`/webui/cluster/start`| *none* |ClusterService instance - `start()` method.|
| POST |`/webui/cluster/stop`| *none* |ClusterService instance - `stop()` method.|


## Security

### Known security issues

- Using transporters (HTTPTransporter, MQTTTransporter) without ssl is dangerous.
- The WebUI and a RestAPI (in `standalone` and `master` instances) can be used without authentication.
- The slave apiKey can be easily stolen via man-in-the-middle attacks.

### Solutions

- Use HTTPS whenever it's possible.
- Block unnecessary port traffic.
- Use VPNs for remote hosts.

## Screenshots

![WebUI Dashboard](https://raw.githubusercontent.com/Gerviba/signal-simulator/master/screenshots/2.png)

![WebUI Dashboard](https://raw.githubusercontent.com/Gerviba/signal-simulator/master/screenshots/3.png)

![WebUI Dashboard](https://raw.githubusercontent.com/Gerviba/signal-simulator/master/screenshots/4.png)

