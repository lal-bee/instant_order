package fun.cyhgraph.utils;

public final class RoleUtil {

    public static final String CHAIRMAN = "2";
    public static final String MANAGER = "1";
    public static final String EMPLOYEE = "0";

    private RoleUtil() {
    }

    public static boolean isChairman(String role) {
        return CHAIRMAN.equals(role);
    }

    public static boolean isManager(String role) {
        return MANAGER.equals(role);
    }

    public static boolean isEmployee(String role) {
        return EMPLOYEE.equals(role);
    }

    public static boolean isValid(String role) {
        return CHAIRMAN.equals(role) || MANAGER.equals(role) || EMPLOYEE.equals(role);
    }

    public static String normalize(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim();
        return isValid(normalized) ? normalized : role;
    }
}
