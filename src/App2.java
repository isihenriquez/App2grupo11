// App2.java - Gestión completa de Cultivos, Parcelas y Actividades
import java.io.*;
import java.util.*;

// Representa una actividad agrícola (riego, fertilización, etc.)
class Actividad {
    private String tipo;
    private String fecha;
    private boolean completada;

    public Actividad(String tipo, String fecha) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.completada = false;
    }

    public String getTipo() { return tipo; }
    public String getFecha() { return fecha; }
    public boolean isCompletada() { return completada; }
    public void completar() { this.completada = true; }

    @Override
    public String toString() {
        return tipo + ":" + fecha + (completada ? ":COMPLETADA" : "");
    }
}

// Representa un cultivo con sus atributos y actividades
class Cultivo {
    private String nombre;
    private String variedad;
    private double superficie;
    private String codigoParcela;
    private String fechaSiembra;
    private String estado;
    private List<Actividad> actividades;

    public Cultivo(String nombre, String variedad, double superficie, String codigoParcela,
                   String fechaSiembra, String estado, List<Actividad> actividades) {
        this.nombre = nombre;
        this.variedad = variedad;
        this.superficie = superficie;
        this.codigoParcela = codigoParcela;
        this.fechaSiembra = fechaSiembra;
        this.estado = estado;
        this.actividades = actividades;
    }

    public String getNombre() { return nombre; }
    public String getVariedad() { return variedad; }
    public double getSuperficie() { return superficie; }
    public String getCodigoParcela() { return codigoParcela; }
    public String getFechaSiembra() { return fechaSiembra; }
    public String getEstado() { return estado; }
    public List<Actividad> getActividades() { return actividades; }
    public void setCodigoParcela(String codigoParcela) { this.codigoParcela = codigoParcela; }

    @Override
    public String toString() {
        return nombre + " (" + variedad + ") - " + estado + " - Parcela: " + codigoParcela;
    }

    // Formato CSV de un cultivo
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cultivo,\"").append(nombre).append("\",\"")
          .append(variedad).append("\",").append(superficie)
          .append(",\"").append(codigoParcela).append("\",\"")
          .append(fechaSiembra).append("\",\"").append(estado).append("\",[");

        for (int i = 0; i < actividades.size(); i++) {
            sb.append("\"").append(actividades.get(i).toString()).append("\"");
            if (i < actividades.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}

// Representa una parcela del campo
class Parcela {
    private String codigo;
    public Parcela(String codigo) { this.codigo = codigo; }
    public String getCodigo() { return codigo; }
    @Override
    public String toString() { return "Parcela: " + codigo; }
}

// Encargado de leer/guardar cultivos desde/hacia un archivo CSV
class CultivoCSVHandler {
    public static List<Cultivo> leerCultivosDesdeCSV(String archivo) {
        List<Cultivo> cultivos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("Cultivo")) {
                    // Separa respetando comillas (regex corregido)
                    String[] partes = linea.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
                    String nombre = partes[1].replace("\"", "");
                    String variedad = partes[2].replace("\"", "");
                    double superficie = Double.parseDouble(partes[3]);
                    String parcela = partes[4].replace("\"", "");
                    String fechaSiembra = partes[5].replace("\"", "");
                    String estado = partes[6].replace("\"", "");

                    // Extrae actividades desde la parte JSON-like
                    String actividadesRaw = linea.substring(linea.indexOf('[') + 1, linea.lastIndexOf(']'));
                    String[] actividadesArray = actividadesRaw.split(",");
                    List<Actividad> actividades = new ArrayList<>();
                    for (String act : actividadesArray) {
                        String clean = act.replace("\"", "").trim();
                        if (clean.contains(":")) {
                            String[] tipoFecha = clean.split(":");
                            if (tipoFecha.length >= 2) {
                                Actividad actividad = new Actividad(tipoFecha[0], tipoFecha[1]);
                                if (tipoFecha.length == 3 && tipoFecha[2].equals("COMPLETADA")) actividad.completar();
                                actividades.add(actividad);
                            }
                        }
                    }
                    cultivos.add(new Cultivo(nombre, variedad, superficie, parcela, fechaSiembra, estado, actividades));
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
        return cultivos;
    }

    public static void guardarCultivosEnCSV(String archivo, List<Cultivo> cultivos) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
            for (Cultivo c : cultivos) {
                pw.println(c.toCSV());
            }
            System.out.println("Cultivos guardados en " + archivo);
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo: " + e.getMessage());
        }
    }
}

// Clase principal de la aplicación agrícola
public class App2 {
// Métodos gestionCultivos, gestionParcelas, gestionActividades, gestionBusquedaReporte, main ya definidos arriba

// Menú de búsqueda y reportes por nombre/variedad o estado de los cultivos
    public static void gestionBusquedaReporte(Scanner sc, List<Cultivo> cultivos) {
        int opcion;
        do {
            System.out.println("=== BÚSQUEDA / REPORTE ===");
            System.out.println("1. Buscar cultivo por nombre o variedad");
            System.out.println("2. Reporte por estado (ACTIVO, COSECHADO, EN_RIESGO)");
            System.out.println("3. Volver");
            System.out.print("Opción: ");
            opcion = sc.nextInt(); sc.nextLine();

            switch (opcion) {
                case 1 -> {
                    // Búsqueda textual por nombre o variedad (case-insensitive)
                    System.out.print("Ingrese nombre o variedad a buscar: ");
                    String criterio = sc.nextLine().toLowerCase();
                    boolean encontrado = false;
                    for (Cultivo c : cultivos) {
                        if (c.getNombre().toLowerCase().contains(criterio) ||
                            c.getVariedad().toLowerCase().contains(criterio)) {
                            System.out.println(c);
                            encontrado = true;
                        }
                    }
                    if (!encontrado) System.out.println("No se encontraron coincidencias.");
                }
                case 2 -> {
                // Agrupación por estado y visualización de cultivos
                    Map<String, List<Cultivo>> porEstado = new HashMap<>();
                    for (Cultivo c : cultivos) {
                        porEstado.putIfAbsent(c.getEstado(), new ArrayList<>());
                        porEstado.get(c.getEstado()).add(c);
                    }
                    for (String estado : porEstado.keySet()) {
                        System.out.println("Estado: " + estado);
                        porEstado.get(estado).forEach(c -> System.out.println("- " + c));
                    }
                }
                case 3 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 3);
    }
    // Método principal: arranca el programa, carga cultivos y gestiona el menú
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java App2 cultivos.csv");
            return;
        }

        String archivoCSV = args[0];
        List<Cultivo> cultivos = CultivoCSVHandler.leerCultivosDesdeCSV(archivoCSV);

        // Genera lista de parcelas únicas a partir de los cultivos
        List<Parcela> parcelas = new ArrayList<>();
        for (Cultivo c : cultivos) {
            if (parcelas.stream().noneMatch(p -> p.getCodigo().equals(c.getCodigoParcela()))) {
                parcelas.add(new Parcela(c.getCodigoParcela()));
            }
        }

        Scanner sc = new Scanner(System.in);
        int opcion;
        do {
            System.out.println("\n=== MENÚ PRINCIPAL ===");
            System.out.println("1. Gestión de Cultivos"); 
            System.out.println("2. Gestión de Parcelas");
            System.out.println("3. Gestión de Actividades");
            System.out.println("4. Búsqueda / Reporte");
            System.out.println("5. Guardar y salir");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt(); sc.nextLine();

            switch (opcion) {
                case 1 -> gestionCultivos(sc, cultivos); // CRUD de cultivos
                case 2 -> gestionParcelas(sc, cultivos, parcelas); // CRUD de parcelas y asignaciones
                case 3 -> gestionActividades(sc, cultivos); // CRUD de actividades
                case 4 -> gestionBusquedaReporte(sc, cultivos); // Reportes y filtros
                case 5 -> CultivoCSVHandler.guardarCultivosEnCSV(archivoCSV, cultivos); // Guardar CSV
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 4);
    }

    // ... (resto del código ya definido)

    public static void gestionCultivos(Scanner sc, List<Cultivo> cultivos) {
        int opcion;
        do {
            System.out.println("=== GESTIÓN DE CULTIVOS ===");
            System.out.println("1. Listar cultivos");
            System.out.println("2. Crear cultivo");
            System.out.println("3. Eliminar cultivo (si no tiene actividades)");
            System.out.println("4. Editar cultivo");
            System.out.println("5. Volver");
            System.out.print("Opción: ");
            opcion = sc.nextInt(); sc.nextLine();

            switch (opcion) {
                case 1 -> {
                    for (int i = 0; i < cultivos.size(); i++) {
                        System.out.println((i + 1) + ". " + cultivos.get(i));
                    }
                }
                case 2 -> {
                    System.out.print("Nombre: "); String nombre = sc.nextLine();
                    System.out.print("Variedad: "); String variedad = sc.nextLine();
                    System.out.print("Superficie: "); double sup = sc.nextDouble(); sc.nextLine();
                    System.out.print("Código parcela: "); String parcela = sc.nextLine();
                    System.out.print("Fecha siembra: "); String fecha = sc.nextLine();
                    System.out.print("Estado: "); String estado = sc.nextLine();
                    List<Actividad> acts = new ArrayList<>();
                    String cont;
                    do {
                        System.out.print("Actividad (tipo:fecha): "); String linea = sc.nextLine();
                        String[] p = linea.split(":");
                        if (p.length >= 2) acts.add(new Actividad(p[0], p[1]));
                        System.out.print("¿Otra? (s/n): "); cont = sc.nextLine();
                    } while (cont.equalsIgnoreCase("s"));
                    cultivos.add(new Cultivo(nombre, variedad, sup, parcela, fecha, estado, acts));
                }
                case 3 -> {
                    System.out.print("Índice a eliminar: "); int ind = sc.nextInt(); sc.nextLine();
                    if (ind >= 1 && ind <= cultivos.size()) {
                        if (cultivos.get(ind - 1).getActividades().isEmpty()) {
                            cultivos.remove(ind - 1);
                            System.out.println("Cultivo eliminado.");
                        } else {
                            System.out.println("No se puede eliminar: tiene actividades asignadas.");
                        }
                    }
                }
                case 4 -> {
                    System.out.print("Índice a editar: "); int i = sc.nextInt(); sc.nextLine();
                    if (i >= 1 && i <= cultivos.size()) {
                        Cultivo c = cultivos.get(i - 1);
                        System.out.print("Nuevo nombre (" + c.getNombre() + "): "); String n = sc.nextLine();
                        System.out.print("Nueva variedad (" + c.getVariedad() + "): "); String v = sc.nextLine();
                        System.out.print("Nueva superficie (" + c.getSuperficie() + "): "); String s = sc.nextLine();
                        System.out.print("Nueva parcela (" + c.getCodigoParcela() + "): "); String p = sc.nextLine();
                        System.out.print("Nueva fecha siembra (" + c.getFechaSiembra() + "): "); String f = sc.nextLine();
                        System.out.print("Nuevo estado (" + c.getEstado() + "): "); String e = sc.nextLine();
                        Cultivo nuevo = new Cultivo(
                            n.isEmpty() ? c.getNombre() : n,
                            v.isEmpty() ? c.getVariedad() : v,
                            s.isEmpty() ? c.getSuperficie() : Double.parseDouble(s),
                            p.isEmpty() ? c.getCodigoParcela() : p,
                            f.isEmpty() ? c.getFechaSiembra() : f,
                            e.isEmpty() ? c.getEstado() : e,
                            c.getActividades());
                        cultivos.set(i - 1, nuevo);
                        System.out.println("Cultivo editado.");
                    }
                }
                case 5 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 5);
    }

    public static void gestionParcelas(Scanner sc, List<Cultivo> cultivos, List<Parcela> parcelas) {
        int opcion;
        do {
            System.out.println("=== GESTIÓN DE PARCELAS ===");
            System.out.println("1. Listar parcelas con cultivos");
            System.out.println("2. Agregar parcela");
            System.out.println("3. Eliminar parcela (si no tiene cultivos activos)");
            System.out.println("4. Asignar cultivo a parcela");
            System.out.println("5. Volver");
            System.out.print("Opción: ");
            opcion = sc.nextInt(); sc.nextLine();

            switch (opcion) {
                case 1 -> {
                    for (Parcela p : parcelas) {
                        System.out.println(p);
                        for (Cultivo c : cultivos) {
                            if (c.getCodigoParcela().equals(p.getCodigo())) {
                                System.out.println("  - " + c);
                            }
                        }
                    }
                }
                case 2 -> {
                    System.out.print("Código nueva parcela: ");
                    String codigo = sc.nextLine();
                    if (parcelas.stream().anyMatch(p -> p.getCodigo().equals(codigo))) {
                        System.out.println("Parcela ya existe.");
                    } else {
                        parcelas.add(new Parcela(codigo));
                        System.out.println("Parcela agregada.");
                    }
                }
                case 3 -> {
                    System.out.print("Código de parcela a eliminar: ");
                    String codigo = sc.nextLine();
                    boolean tieneCultivos = cultivos.stream().anyMatch(c -> c.getCodigoParcela().equals(codigo) && c.getEstado().equals("ACTIVO"));
                    if (tieneCultivos) {
                        System.out.println("No se puede eliminar: tiene cultivos activos.");
                    } else {
                        parcelas.removeIf(p -> p.getCodigo().equals(codigo));
                        System.out.println("Parcela eliminada.");
                    }
                }
                case 4 -> {
                    for (int i = 0; i < cultivos.size(); i++) {
                        System.out.println((i + 1) + ". " + cultivos.get(i));
                    }
                    System.out.print("Seleccione cultivo a asignar: ");
                    int idx = sc.nextInt(); sc.nextLine();
                    if (idx >= 1 && idx <= cultivos.size()) {
                        System.out.print("Nuevo código de parcela: ");
                        String nueva = sc.nextLine();
                        cultivos.get(idx - 1).setCodigoParcela(nueva);
                        if (parcelas.stream().noneMatch(p -> p.getCodigo().equals(nueva))) {
                            parcelas.add(new Parcela(nueva));
                        }
                        System.out.println("Cultivo asignado a nueva parcela.");
                    }
                }
                case 5 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 5);
    }

    public static void gestionActividades(Scanner sc, List<Cultivo> cultivos) {
        int opcion;
        do {
            System.out.println("=== GESTIÓN DE ACTIVIDADES ===");
            System.out.println("1. Registrar actividad");
            System.out.println("2. Listar actividades por cultivo");
            System.out.println("3. Eliminar actividad");
            System.out.println("4. Marcar actividad como completada");
            System.out.println("5. Volver");
            System.out.print("Opción: ");
            opcion = sc.nextInt(); sc.nextLine();
