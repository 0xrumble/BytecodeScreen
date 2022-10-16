package services;

import basic_class.ClassReference;
import inher.InheritanceMap;
import inher.InheritanceUtil;
import org.slf4j.Logger;

import java.util.Map;

public class InheritanceService {

    private static Logger logger;

    public static InheritanceMap start(Map<ClassReference.Handle, ClassReference> classMap) {
        return InheritanceUtil.derive(classMap);
    }
}
