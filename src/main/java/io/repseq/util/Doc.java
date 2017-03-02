package io.repseq.util;

import java.lang.annotation.*;

/**
 * Annotation used in enums and constants to add runtime-readable documentation
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Doc {
    String value();
}
