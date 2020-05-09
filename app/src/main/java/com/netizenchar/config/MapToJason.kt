package com.netizenchar.config

class MapToJason {
  fun map(hashMap: HashMap<String, String>): String {
    return hashMap.toString().replace(", ", "&").replace("{", "").replace("}", "")
  }
}