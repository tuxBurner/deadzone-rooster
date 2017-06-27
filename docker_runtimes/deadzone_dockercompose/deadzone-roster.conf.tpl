include "application.conf"

db {
  default.driver  = "org.mariadb.jdbc.Driver"
  default.url = "jdbc:mariadb://mariadb:3306/deadzone"
  default.username = "deadzone"
  default.password = "deadzone"
}

play.evolutions.autoApply = true

deadzone {
  reinitData = true
  cacheTimeOut = 15
}

play.crypto.secret="changemeDeadzoneRoster"