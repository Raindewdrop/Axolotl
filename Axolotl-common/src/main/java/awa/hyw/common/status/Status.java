package awa.hyw.common.status;

/**
 * Status codes for injection process
 */
public enum Status {
    // ===== Loader phase (1xxx) =====
    /* ServiceMain Phase */
    LOADER_INIT(1000, "Initializing loader"),
    LOADER_SEARCH_MINECRAFT(1100, "Searching for Minecraft process"),
    LOADER_FOUND_MINECRAFT(1101, "Found Minecraft process"),
    LOADER_CONNECT_PROCESS(1300, "Connecting to target process"),

    /* Bootstrap Phase */
    LOADER_FINDING_CLASSLOADER(1200, "Waiting for client thread"),
    LOADER_EXTRACTING_RESOURCES(1201, "Extracting resources"),
    LOADER_SETUP_CLASSLOADER(1202, "Setting up classloader"),
    LOADER_START_CORE(1400, "Invoking start function"),

    // ===== Core phase (2xxx) =====
    CORE_SETUP_MAPPING(2002, "Setting up mapping"),
    CORE_WAIT_INIT_CALLBACK(2005, "Waiting for Hook callback"),
    CORE_PATCH(2003, "Processing Mixins"),

    // ===== Special codes (9xxx) =====
    SUCCESS(9000, "Successfully completed"),
    ERROR_GENERAL(9900, "An error occurred"),
    ERROR_CONNECTION(9901, "Failed to connect to game process"),
    ERROR_INJECTION(9902, "Injection failed");

    private final int code;
    private final String description;

    /**
     * Constructor
     *
     * @param code        Status code
     * @param description Status description
     */
    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get enum instance from code
     */
    public static Status fromCode(int code) {
        for (Status status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
