IPCamp Competition - Signal Simulator
===

A solution by [Szabó Gergely](https://github.com/Gerviba).

## Build, test, setup

- Show in the IDE: You need to have [Lombok](https://projectlombok.org/setup/eclipse) installed.
- Build: `$ mvn clean install`
- Test: `$ mvn test`
- Setup: The compiled jar file is executable. (Thanks to spring-boot)

## Configuration

Custom config file are available for every profiles.

- `src/main/resources/application.properties` : Global settings
- `src/main/resources/application-<mode>.properties`

## Profiles

|Profile name|Description|
|------------|-----------|
|standalone|Simple behaviour.|
|master|Accepts and discovers slaves. Manages the slave nodes.|
|slave|Works as a slave node.|
|test|Used for tests. Don't use it in production.|

#### How to set profiles?

Edit the `application.properties` file or use the `-Dspring.profiles.active=` command line arguments.

## RestAPI

### All (incl. standalone)


|Method|Path|Arguments|Description|
|:----:|----|---------|-----------|
| GET  |`/api/status/server`| *none* |Returns status like in the |

### Master

To enable: use profile `master`

|Method|Path|Arguments|Description|
|:----:|----|---------|-----------|
| POST |`/api/slave`|`apiKey:String`, `port:Integer`|Registers slave node|
|DELETE|`/api/slave`|`apiKey:String`, `port:Integer`|Unregisters slave node|

### Slave

To enable: use profile `slave`

|Method|Path|Arguments|Description|
|:----:|----|---------|-----------|
| GET  |`/api/slave/status`|`apiKey:String`|Results statistic from this node|
| POST |`/api/slave/start`|`command:CommandDao`|Send command to node|
| POST |`/api/slave/stop`| *none* |Send stop signal|

## WebUI

To enable: use profile `standalone` or `master`.<br>
**NOTE**: The config property `simulator.enable-webui` must be true.

#### Dependencies

- JavaScript, JQuery : Client side
- Google fonts, Google material icons : Client side
- Spring MVC, Thymeleaf : Server side

#### Endpoints

|Method|Path|Description|
|:----:|----|-----------|
| GET  |`/`|Redirection to /webui|
| GET  |`/webui/`|WebUI dashboard, realtime statistics|
| GET  |`/webui/controls`|Input generator (ON/OFF)|
| GET  |`/webui/settings`|Vehicle count|
| GET  |`/webui/cluster`|List and stats of slave nodes|

#### AJAX

|Method|Path|Arguments|Description|
|:----:|----|---------|-----------|
| POST |`/webui/range`|`name:String`, `count:Long`|Set the count of the selected vehicle|
| POST |`/webui/controls/start`| *none* |It will call the InputSource instance's `start()` method.|
| POST |`/webui/controls/stop`| *none* |It will call the InputSource instance's `stop()` method.|
| GET  |`/webui/controls/status`| *none* |It will call the InputSource instance's isRunning() method. Results: `{'status':'ON' / 'OFF'}`|

## Todo

- Total control over slaves
- Ajax webUI
- Integration tests
- RestInputSource
- File input source
