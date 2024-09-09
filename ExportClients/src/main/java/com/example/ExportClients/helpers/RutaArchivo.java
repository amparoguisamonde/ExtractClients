package com.example.ExportClients.helpers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

public class RutaArchivo {

    public static Path crearRuta(String directorio, String nombreArchivo) throws IOException {
        Path rutaDirectorio = Paths.get(directorio);

        if (!Files.exists(rutaDirectorio)) {
            Files.createDirectories(rutaDirectorio);
        }

        return rutaDirectorio.resolve(nombreArchivo);
    }
}

