/**
 *  Filtrete 3M-50 WiFi Thermostat
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
 *  https://github.com/statusbits/smartthings/blob/master/RadioThermostat/RadioThermostat.device.groovy
 *
 *  Revision History
 *  ----------------
 *  2014-08-12: Version: 0.9.0
 */

import groovy.json.JsonSlurper

preferences {
    input("confIpAddr", "string", title:"Thermostat IP Address",
        required:true, displayDuringSetup: true)
    input("confTcpPort", "number", title:"Thermostat TCP Port",
        defaultValue:"80", required:true, displayDuringSetup:true)
    input("confAwayHeat", "number", title:"Away Heating Setpoint",
        defaultValue:"62", required:true, displayDuringSetup:true)
    input("confAwayCool", "number", title:"Away Cooling Setpoint",
        defaultValue:"84", required:true, displayDuringSetup:true)
}

metadata {
    definition (name:"Radio Thermostat", namespace:"statusbits", author:"geko@statusbits.com") {
        capability "Thermostat"
        capability "Temperature Measurement"
        capability "Sensor"
        capability "Refresh"
        capability "Polling"

        // Custom attributes
        attribute "fanState", "string"  // Fan operating state. Values: "on", "off"
        attribute "hold", "string"      // Target temperature Hold status. Values: "on", "off"
        attribute "away", "string"      // Away status. Values: "on", "off"

        // Custom commands
        command "heatLevelUp"
        command "heatLevelDown"
        command "coolLevelUp"
        command "coolLevelDown"
        command "holdOn"
        command "holdOff"
        command "awayOn"
        command "awayOff"
    }

    tiles {
        valueTile("temperature", "device.temperature") {
            state "temperature", label:'${currentValue}°', unit:"F",
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
        }

        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel:false) {
            state "default", label:'${currentValue}°', unit:"F",
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
        }

        valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel:false) {
            state "default", label:'${currentValue}°', unit:"F",
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
        }

        standardTile("heatLevelUp", "device.heatingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Heating', icon:"st.custom.buttons.add-icon", action:"heatLevelUp"
        }

        standardTile("heatLevelDown", "device.heatingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Heating', icon:"st.custom.buttons.subtract-icon", action:"heatLevelDown"
        }

        standardTile("coolLevelUp", "device.coolingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Cooling', icon:"st.custom.buttons.add-icon", action:"coolLevelUp"
        }

        standardTile("coolLevelDown", "device.coolingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Cooling', icon:"st.custom.buttons.subtract-icon", action:"coolLevelDown"
        }

        standardTile("operatingState", "device.thermostatOperatingState", inactiveLabel:false, decoration:"flat") {
            state "default", label:'[State]'
            state "idle", label:'', icon:"st.thermostat.heating-cooling-off"
            state "heating", label:'', icon:"st.thermostat.heating"
            state "cooling", label:'', icon:"st.thermostat.cooling"
        }

        standardTile("fanState", "device.fanState", inactiveLabel:false, decoration:"flat") {
            state "default", label:'[Fan State]'
            state "on", label:'', icon:"st.thermostat.fan-on"
            state "off", label:'', icon:"st.thermostat.fan-off"
        }

        standardTile("mode", "device.thermostatMode", inactiveLabel:false) {
            state "default", label:'[Mode]'
            state "off", label:'', icon:"st.thermostat.heating-cooling-off", backgroundColor:"#FFFFFF", action:"thermostat.heat"
            state "heat", label:'', icon:"st.thermostat.heat", backgroundColor:"#FFCC99", action:"thermostat.cool"
            state "cool", label:'', icon:"st.thermostat.cool", backgroundColor:"#99CCFF", action:"thermostat.auto"
            state "auto", label:'', icon:"st.thermostat.auto", backgroundColor:"#99FF99", action:"thermostat.off"
        }

        standardTile("fanMode", "device.thermostatFanMode", inactiveLabel:false) {
            state "default", label:'[Fan Mode]'
            state "auto", label:'', icon:"st.thermostat.fan-auto", backgroundColor:"#A4FCA6", action:"thermostat.fanCirculate"
            state "circulate", label:'', icon:"st.thermostat.fan-circulate", backgroundColor:"#CEFCA4", action:"thermostat.fanOn"
            state "on", label:'', icon:"st.thermostat.fan-on", backgroundColor:"#FAFCA4", action:"thermostat.fanAuto"
        }

        standardTile("hold", "device.hold", inactiveLabel:false) {
            state "default", label:'[Hold]'
            state "on", label:'Hold On', backgroundColor:"#FFDB94", action:"holdOff"
            state "off", label:'Hold Off', backgroundColor:"#FFFFFF", action:"holdOn"
        }

        standardTile("away", "device.away", inactiveLabel:false) {
            state "default", label:'[Away]'
            state "on", label:'Away On', backgroundColor:"#FFDB94", action:"awayOff"
            state "off", label:'Away Off', backgroundColor:"#FFFFFF", action:"awayOn"
        }

        standardTile("refresh", "device.thermostatMode", inactiveLabel:false, decoration:"flat") {
            state "default", icon:"st.secondary.refresh", action:"refresh.refresh"
        }

        main(["temperature", "mode"])

        details(["temperature", "operatingState", "fanState",
            "heatingSetpoint", "heatLevelDown", "heatLevelUp",
            "coolingSetpoint", "coolLevelDown", "coolLevelUp",
            "mode", "fanMode", "hold", "away", "refresh"])
    }

    simulator {
        status "Temperature 72.0":      "simulator:true, temp:72.00"
        status "Cooling Setpoint 76.0": "simulator:true, t_cool:76.00"
        status "Heating Setpoint 68.0": "simulator:true, t_cool:68.00"
        status "Thermostat Mode Off":   "simulator:true, tmode:0"
        status "Thermostat Mode Heat":  "simulator:true, tmode:1"
        status "Thermostat Mode Cool":  "simulator:true, tmode:2"
        status "Thermostat Mode Auto":  "simulator:true, tmode:3"
        status "Fan Mode Auto":         "simulator:true, fmode:0"
        status "Fan Mode Circulate":    "simulator:true, fmode:1"
        status "Fan Mode On":           "simulator:true, fmode:2"
        status "State Off":             "simulator:true, tstate:0"
        status "State Heat":            "simulator:true, tstate:1"
        status "State Cool":            "simulator:true, tstate:2"
        status "Fan State Off":         "simulator:true, fstate:0"
        status "Fan State On":          "simulator:true, fstate:1"
        status "Hold Disabled":         "simulator:true, hold:0"
        status "Hold Enabled":          "simulator:true, hold:1"
    }
}

def parse(String message) {
    TRACE("parse(${message})")

    def msg = stringToMap(message)

    if (msg.headers) {
        // parse HTTP response headers
        def headers = new String(msg.headers.decodeBase64())
        def parsedHeaders = parseHttpHeaders(headers)
        TRACE("parsedHeaders: ${parsedHeaders}")
        if (parsedHeaders.status != 200) {
            log.error "Server error: ${parsedHeaders.reason}"
            return null
        }

        // parse HTTP response body
        if (!msg.body) {
            log.error "HTTP response has no body"
            return null
        }

        def body = new String(msg.body.decodeBase64())
        def slurper = new JsonSlurper()
        def tstat = slurper.parseText(body)

        return parseTstatData(tstat)
    } else if (msg.containsKey("simulator")) {
        // simulator input
        return parseTstatData(msg)
    }

    return null
}

// thermostat.setThermostatMode
def setThermostatMode(mode) {
    TRACE("setThermostatMode(${mode})")

    switch (mode) {
    case "off":             return off()
    case "heat":            return heat()
    case "cool":            return cool()
    case "auto":            return auto()
    case "emergency heat":  return emergencyHeat()
    }

    log.error "Invalid thermostat mode: \'${mode}\'"
}

// thermostat.off
def off() {
    TRACE("off()")

    if (device.currentValue("thermostatMode") == "off") {
        return null
    }

    sendEvent([name:"thermostatMode", value:"off"])
    return writeTstatValue('tmode', 0)
}

// thermostat.heat
def heat() {
    TRACE("heat()")

    if (device.currentValue("thermostatMode") == "heat") {
        return null
    }

    sendEvent([name:"thermostatMode", value:"heat"])
    return writeTstatValue('tmode', 1)
}

// thermostat.cool
def cool() {
    TRACE("cool()")

    if (device.currentValue("thermostatMode") == "cool") {
        return null
    }

    sendEvent([name:"thermostatMode", value:"cool"])
    return writeTstatValue('tmode', 2)
}

// thermostat.auto
def auto() {
    TRACE("auto()")

    if (device.currentValue("thermostatMode") == "auto") {
        return null
    }

    sendEvent([name:"thermostatMode", value:"auto"])
    return writeTstatValue('tmode', 3)
}

// thermostat.emergencyHeat
def emergencyHeat() {
    TRACE("emergencyHeat()")
    log.warn "'emergency heat' mode is not supported"
}

// thermostat.setThermostatFanMode
def setThermostatFanMode(fanMode) {
    TRACE("setThermostatFanMode(${fanMode})")

    switch (fanMode) {
    case "auto":        return fanAuto()
    case "circulate":   return fanCirculate()
    case "on":          return fanOn()
    }

    log.error "Invalid fan mode: \'${fanMode}\'"
}

// thermostat.fanAuto
def fanAuto() {
    TRACE("fanAuto()")

    if (device.currentValue("thermostatFanMode") == "fanAuto") {
        return null
    }

    sendEvent([name:"thermostatFanMode", value:"fanAuto"])
    return writeTstatValue('fmode', 0)
}

// thermostat.fanCirculate
def fanCirculate() {
    TRACE("fanCirculate()")

    if (device.currentValue("thermostatFanMode") == "fanCirculate") {
        return null
    }

    sendEvent([name:"thermostatFanMode", value:"fanCirculate"])
    return writeTstatValue('fmode', 1)
}

// thermostat.fanOn
def fanOn() {
    TRACE("fanOn()")

    if (device.currentValue("thermostatFanMode") == "fanOn") {
        return null
    }

    sendEvent([name:"thermostatFanMode", value:"fanOn"])
    return writeTstatValue('fmode', 2)
}

// thermostat.setHeatingSetpoint
def setHeatingSetpoint(tempHeat) {
    TRACE("setHeatingSetpoint(${tempHeat})")

    def ev = [
        name:   "heatingSetpoint",
        value:  tempHeat,
        unit:   getTemperatureScale(),
    ]

    sendEvent(ev)

    if (getTemperatureScale() == "C") {
        tempHeat = temperatureCtoF(tempHeat)
    }

    return writeTstatValue('it_heat', tempHeat)
}

// thermostat.setCoolingSetpoint
def setCoolingSetpoint(tempCool) {
    TRACE("setCoolingSetpoint(${tempCool})")

    def ev = [
        name:   "coolingSetpoint",
        value:  tempCool,
        unit:   getTemperatureScale(),
    ]

    sendEvent(ev)

    if (getTemperatureScale() == "C") {
        tempCool = temperatureCtoF(tempCool)
    }

    return writeTstatValue('it_cool', tempCool)
}

def heatLevelDown() {
    TRACE("heatLevelDown()")

    def currentT = device.currentValue("heatingSetpoint")?.toFloat()
    if (!currentT) {
        return
    }

    def limit = 50
    def step = 1
    if (getTemperatureScale() == "C") {
        limit = 10
        step = 0.5
    }

    if (currentT > limit) {
        setHeatingSetpoint(currentT - step)
    }
}

def heatLevelUp() {
    TRACE("heatLevelUp()")

    def currentT = device.currentValue("heatingSetpoint")?.toFloat()
    if (!currentT) {
        return
    }

    def limit = 95
    def step = 1
    if (getTemperatureScale() == "C") {
        limit = 35
        step = 0.5
    }

    if (currentT < limit) {
        setHeatingSetpoint(currentT + step)
    }
}

def coolLevelDown() {
    TRACE("coolLevelDown()")

    def currentT = device.currentValue("coolingSetpoint")?.toFloat()
    if (!currentT) {
        return
    }

    def limit = 50
    def step = 1
    if (getTemperatureScale() == "C") {
        limit = 10
        step = 0.5
    }

    if (currentT > limit) {
        setCoolingSetpoint(currentT - step)
    }
}

def coolLevelUp() {
    TRACE("coolLevelUp()")

    def currentT = device.currentValue("coolingSetpoint")?.toFloat()
    if (!currentT) {
        return
    }

    def limit = 95
    def step = 1
    if (getTemperatureScale() == "C") {
        limit = 35
        step = 0.5
    }

    if (currentT < limit) {
        setCoolingSetpoint(currentT + step)
    }
}

def holdOn() {
    TRACE("holdOn()")

    if (device.currentValue("hold") == "on") {
        return null
    }

    sendEvent([name:"hold", value:"on"])
    writeTstatValue("hold", 1)
}

def holdOff() {
    TRACE("holdOff()")

    if (device.currentValue("hold") == "off") {
        return null
    }

    sendEvent([name:"hold", value:"off"])
    writeTstatValue("hold", 0)
}

def awayOn() {
    TRACE("awayOn()")
}

def awayOff() {
    TRACE("awayOff()")
}

// polling.poll 
def poll() {
    TRACE("poll()")
    refresh()
}

// refresh.refresh
def refresh() {
    TRACE("refresh()")
    setNetworkId(confIpAddr, confTcpPort)
    apiGet("/tstat")
}

// Sets device Network ID in 'AAAAAAAA:PPPP' format
private String setNetworkId(ipaddr, port) { 
    TRACE("setNetworkId(${ipaddr}, ${port})")

    def hexIp = ipaddr.tokenize('.').collect {
        String.format('%02X', it.toInteger())
    }.join()

    def hexPort = String.format('%04X', port.toInteger())
    device.deviceNetworkId = "${hexIp}:${hexPort}"
    log.debug "device.deviceNetworkId = ${device.deviceNetworkId}"
}

private apiGet(String path) {
    TRACE("apiGet(${path})")

    def headers = [
        HOST:       "${confIpAddr}:${confTcpPort}",
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'GET',
        path:       path,
        headers:    headers
    ]

    return new physicalgraph.device.HubAction(httpRequest)
}

private apiPost(String path, data) {
    TRACE("apiPost(${path}, ${data})")

    def headers = [
        HOST:       "${confIpAddr}:${confTcpPort}",
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'POST',
        path:       path,
        headers:    headers,
        body:       data
    ]

    return new physicalgraph.device.HubAction(httpRequest)
}

private def writeTstatValue(name, value) {
    TRACE("writeTstatValue(${name}, ${value})")

    setNetworkId(confIpAddr, confTcpPort)

    def json = "{\"${name}\": ${value}}"
    def hubActions = [
        apiPost("/tstat", json),
        delayHubAction(2000),
        apiGet("/tstat")
    ]

    return hubActions
}

private def delayHubAction(ms) {
    return new physicalgraph.device.HubAction("delay ${ms}")
}

private parseHttpHeaders(String headers) {
    def lines = headers.readLines()
    def status = lines.remove(0).split()

    def result = [
        protocol:   status[0],
        status:     status[1].toInteger(),
        reason:     status[2]
    ]

    return result
}

private def parseTstatData(Map tstat) {
    TRACE("parseTstatData(${tstat})")

    def events = []
    if (tstat.containsKey("error_msg")) {
        log.error "Thermostat error: ${tstat.error_msg}"
        return null
    }

    if (tstat.containsKey("success")) {
        // this is POST response - ignore
        return null
    }

    if (tstat.containsKey("temp")) {
        //Float temp = tstat.temp.toFloat()
        def ev = [
            name:   "temperature",
            value:  scaleTemperature(tstat.temp.toFloat()),
            unit:   getTemperatureScale(),
        ]

        events << createEvent(ev)
    }

    if (tstat.containsKey("t_cool")) {
        def ev = [
            name:   "coolingSetpoint",
            value:  scaleTemperature(tstat.t_cool.toFloat()),
            unit:   getTemperatureScale(),
        ]

        events << createEvent(ev)
    }

    if (tstat.containsKey("t_heat")) {
        def ev = [
            name:   "heatingSetpoint",
            value:  scaleTemperature(tstat.t_heat.toFloat()),
            unit:   getTemperatureScale(),
        ]

        events << createEvent(ev)
    }

    if (tstat.containsKey("tstate")) {
        def tstate = parseThermostatState(tstat.tstate)
        //TRACE("tstate: ${tstate}")
        if (device.currentState("thermostatOperatingState")?.value != tstate) {
            def ev = [
                name:   "thermostatOperatingState",
                value:  tstate
            ]

            events << createEvent(ev)
        }
    }

    if (tstat.containsKey("fstate")) {
        def fstate = parseFanState(tstat.fstate)
        //TRACE("fstate: ${fstate}")
        if (device.currentState("fanState")?.value != fstate) {
            def ev = [
                name:   "fanState",
                value:  fstate
            ]

            events << createEvent(ev)
        }
    }

    if (tstat.containsKey("tmode")) {
        def tmode = parseThermostatMode(tstat.tmode)
        //TRACE("tmode: ${tmode}")
        if (device.currentState("thermostatMode")?.value != tmode) {
            def ev = [
                name:   "thermostatMode",
                value:  tmode
            ]

            events << createEvent(ev)
        }
    }

    if (tstat.containsKey("fmode")) {
        def fmode = parseFanMode(tstat.fmode)
        //TRACE("fmode: ${fmode}")
        if (device.currentState("thermostatFanMode")?.value != fmode) {
            def ev = [
                name:   "thermostatFanMode",
                value:  fmode
            ]

            events << createEvent(ev)
        }
    }

    if (tstat.containsKey("hold")) {
        def hold = parseThermostatHold(tstat.hold)
        //TRACE("hold: ${hold}")
        if (device.currentState("hold")?.value != hold) {
            def ev = [
                name:   "hold",
                value:  hold
            ]

            events << createEvent(ev)
        }
    }

    TRACE("events: ${events}")
    return events
}

private def parseThermostatState(val) {
    def values = [
        "idle",     // 0
        "heating",  // 1
        "cooling"   // 2
    ]

    return values[val.toInteger()]
}

private def parseFanState(val) {
    def values = [
        "off",      // 0
        "on"        // 1
    ]

    return values[val.toInteger()]
}

private def parseThermostatMode(val) {
    def values = [
        "off",      // 0
        "heat",     // 1
        "cool",     // 2
        "auto"      // 3
    ]

    return values[val.toInteger()]
}

private def parseFanMode(val) {
    def values = [
        "auto",     // 0
        "circulate",// 1
        "on"        // 2
    ]

    return values[val.toInteger()]
}

private def parseThermostatHold(val) {
    def values = [
        "off",      // 0
        "on"        // 1
    ]

    return values[val.toInteger()]
}

private def scaleTemperature(Float temp) {
    if (getTemperatureScale() == "C") {
        return temperatureFtoC(temp)
    }

    return temp.round(1)
}

private def temperatureCtoF(Float tempC) {
    Float t = (tempC * 1.8) + 32
    return t.round(1)
}

private def temperatureFtoC(Float tempF) {
    Float t = (tempF - 32) / 1.8
    return t.round(1)
}

private def TRACE(message) {
    log.debug message
}
