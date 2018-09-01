package killteam.logic;

/**
 * Represents a weapon type
 */
public enum EWeaponType {
    RAPIDFIRE("Schnellfeuer"),
    STORM("Sturm"),
    PISTOL("Pistole"),
    FIGHT("Nahkampf"),
    Heavy("Schwer"),
    GRANADE("Granate")
    ;

    /**
     * The name of the weapon type in the csv
     */
    private final String csvWeapontypeString;

    EWeaponType(String csvWeapontypeString) {

        this.csvWeapontypeString = csvWeapontypeString;
    }

    /**
     * Find the weapon type via the csv type
     * @param csvWeapontypeString the type from the csv
     * @return null when not found or the weapontype
     */
    public static EWeaponType findWeaponTypeByCsv(String csvWeapontypeString) {
        for(EWeaponType weaponType : EWeaponType.values()) {
            if(weaponType.csvWeapontypeString.equals(csvWeapontypeString)) {
                return weaponType;
            }
        }

        return null;
    }
}
