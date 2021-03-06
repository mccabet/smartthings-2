/**
 *  Virtual Illuminance Tile
 *
 *  Copyright (c) 2014 Statusbits.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License. You may obtain a
 *  copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License  for the specific language governing permissions and limitations
 *  under the License.
 *
 *  The latest version of this file can be found at:
 *  https://github.com/statusbits/smartthings/blob/master/VirtualThings/VirtualIlluminanceTile.device.groovy
 *
 *  Revision History
 *  ----------------
 *  2014-08-28  V1.1.0  parse() takes 'illuminance:<value>' as an argument
 *  2014-08-11  V1.0.0  Initial release
 */

metadata {
    definition (name:"Virtual Illuminance Tile", namespace:"statusbits", author:"geko@statusbits.com") {
        capability "Illuminance Measurement"
        capability "Sensor"

        // custom commands
        command "parse"     // (String "illuminance:<value>")
    }

    tiles {
        valueTile("illuminance", "device.illuminance", width:2, height:2) {
            state "luminosity", label:'${currentValue} ${unit}', unit:"lux"
        }

        main(["illuminance"])
        details(["illuminance"])
    }

    simulator {
        for (int i = 10; i < 100; i += 20) {
            status "Illuminance ${i} lux": "illuminance:${i}"
        }
        for (int i = 100; i < 1000; i += 200) {
            status "Illuminance ${i} lux": "illuminance:${i}"
        }
    }
}

def parse(String message) {
    TRACE("parse(${message})")

    Map msg = stringToMap(message)
    if (!msg.containsKey("illuminance")) {
        log.error "Invalid message: ${message}"
        return null
    }

    def event = [
        name  : "illuminance",
        value : msg.illuminance,
        unit  : "lux"
    ]

    TRACE("event: (${event})")
    sendEvent(event)
}

private def TRACE(message) {
    //log.debug message
}
