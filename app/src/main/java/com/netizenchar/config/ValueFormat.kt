package com.netizenchar.config

import java.math.BigDecimal

class ValueFormat {
  private var longFormat = BigDecimal(100000000)
  private var bigDecimalFormat = BigDecimal(0.00000001)

  fun dogeToDecimal(value: BigDecimal) : BigDecimal {
    return value.multiply(longFormat).setScale(0, BigDecimal.ROUND_HALF_DOWN)
  }

  fun decimalToDoge(value: BigDecimal) : BigDecimal {
    return value.multiply(bigDecimalFormat).setScale(8, BigDecimal.ROUND_HALF_DOWN)
  }
}