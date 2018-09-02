package controllers

object FixMessagesKey {

  def fixKey(keyToFix: String): String = {
    keyToFix
      .toLowerCase
      .replace("ü", "ue")
      .replace("ä", "ae")
      .replace("ö", "oe")
      .replace("ß", "ss")
      .replace(" ", "")
      .replace("-","")
      .replace("'","")
      .replace("!","")
  }

}
