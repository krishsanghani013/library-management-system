package library;


public class Student {
    private final int id;
    private final String name;
    private final String course;
    private final String departmentCode;

    public Student(int id, String name, String course, String departmentCode) {
        if (id <= 0) {
            throw new DataStoreFormatException("Student id must be positive. Found: " + id);
        }
        if (name == null || name.isBlank()) {
            throw new DataStoreFormatException("Student name cannot be blank for id " + id);
        }
        if (course == null || course.isBlank()) {
            throw new DataStoreFormatException("Student course cannot be blank for id " + id);
        }
        if (departmentCode == null || departmentCode.isBlank()) {
            throw new DataStoreFormatException("Department code cannot be blank for student id " + id);
        }

        this.id = id;
        this.name = name.trim().replaceAll("\\s+", " ");
        this.course = course.trim();
        this.departmentCode = departmentCode.trim().toUpperCase();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCourse() {
        return course;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }
}


