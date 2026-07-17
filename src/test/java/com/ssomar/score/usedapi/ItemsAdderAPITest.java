package com.ssomar.score.usedapi;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class ItemsAdderAPITest {

    /**
     * The purpose of this test is to prevent developers from calling these method calls. Further context is written at
     * {@link ItemsAdderAPI}
     */
    @org.junit.jupiter.api.Test
    void breakCustomBlock() {
        // asked chatgpt for help. might be wrong
        JavaClasses classes = new ClassFileImporter()
                .importPackages("com.ssomar.score.usedapi");

        Set<String> forbidden = new HashSet<>(Arrays.asList("breakEB", "breakBlock", "runBreakBlockAnimation"));

        ArchCondition<JavaMethod> noBreakEBCall =
                new ArchCondition<JavaMethod>("must not call breakEB") {
                    @Override
                    public void check(JavaMethod method, ConditionEvents events) {
                        method.getMethodCallsFromSelf().forEach(call -> {
                            if (forbidden.contains(call.getTarget().getName())) {
                                String msg = method.getFullName()
                                        + " calls forbidden method "
                                        + call.getTarget().getName()
                                        + " at "
                                        + call.getOrigin().getSourceCodeLocation();

                                events.add(SimpleConditionEvent.violated(method, msg));
                            }
                        });
                    }
                };

        ArchRule rule = ArchRuleDefinition.methods()
                .that().haveName("breakCustomBlock")
                .and().areDeclaredIn(ItemsAdderAPI.class)
                .should(noBreakEBCall);

        rule.check(classes);
    }
}