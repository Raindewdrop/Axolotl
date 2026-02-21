package awa.hyw.Axolotl;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import awa.hyw.Axolotl.event.impl.EntryEvent;
import awa.hyw.Axolotl.injection.patch.MinecraftPatch;
import awa.hyw.Axolotl.misc.ClassUtil;
import awa.hyw.common.status.Status;
import awa.hyw.common.status.StatusReporter;
import awa.hyw.patchify.Mapping;
import awa.hyw.patchify.PatchLoader;
import awa.hyw.patchify.asm.MethodWrapper;
import awa.hyw.patchify.asm.ReflectionUtil;
import kotlin.Unit;

public class Entry {
    private static final Logger log = LogManager.getLogger(Entry.class);
    private static final List<Class<?>> PATCHES = new ArrayList<>();

    public static void entry(Instrumentation inst, boolean obfuscated, Map<String, byte[]> classes) throws Throwable {
        // Init status reporter
        try {
            initStatusReporter();
            Axolotl.INSTANCE.setStatusReporter(status -> {
                StatusReporter.report(status);
                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            log.warn("Failed to initialize status reporter: {}", e.getMessage());
            // Continue execution even if status reporter fails
            Axolotl.INSTANCE.setStatusReporter(status -> Unit.INSTANCE);
        }

        log.info("Initializing Axolotl...");
        ClassUtil.init(inst);
        Axolotl.INSTANCE.setClasses(classes);
        Axolotl.INSTANCE.setObfuscated(obfuscated);
        if (obfuscated) {
            Axolotl.INSTANCE.getStatusReporter().invoke(Status.CORE_SETUP_MAPPING);
            log.info("Axolotl running in obfuscated mode");
            InputStream resource = Entry.class.getResourceAsStream("/assets/Axolotl/mapping.srg");
            if (resource == null) {
                log.error("Mapping file not found, please provide mapping.srg in the root directory of the jar file");
                reportError(Status.ERROR_GENERAL);
                throw new RuntimeException("Mapping file not found");
            }
            Mapping mapping0 = new Mapping(resource.readAllBytes());
            log.info("Mapping loaded: Classes: {}, Methods: {}, Fields: {}",
                    mapping0.classesMapping.size(), mapping0.methodsMapping.size(), mapping0.fieldMapping.size());
            PatchLoader.mapping = mapping0;
            MethodWrapper.mapping = mapping0;
            ReflectionUtil.mapping = mapping0;
            resource.close();
        }

        PatchLoader.INSTANCE.loadPatch(MinecraftPatch.class, ClassUtil::getClassBytes, ClassUtil::redefineClass);
        Axolotl.INSTANCE.getStatusReporter().invoke(Status.CORE_WAIT_INIT_CALLBACK);
        log.info("Entry point execution completed, waiting for Minecraft tick to initialize Axolotl");
        new EntryEvent().post();
    }

    /**
     * Init status reporter
     */
    private static void initStatusReporter() throws Exception {
        // Try to read port from system properties
        String portStr = System.getProperty("Axolotl.status.port");
        if (portStr != null && !portStr.isEmpty()) {
            try {
                int port = Integer.parseInt(portStr);
                StatusReporter.init(port);
                log.info("Status reporter initialized on port {}", port);
            } catch (NumberFormatException e) {
                log.warn("Invalid port number: {}", portStr);
                throw e;
            }
        } else throw new Exception("Axolotl status port not found in system properties");
    }

    /**
     * Report error status
     */
    private static void reportError(Status errorStatus) {
        try {
            StatusReporter.report(errorStatus);
        } catch (Exception e) {
            log.warn("Exception occurred while reporting error status: {}", e.getMessage());
        }
    }
}


