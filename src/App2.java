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
