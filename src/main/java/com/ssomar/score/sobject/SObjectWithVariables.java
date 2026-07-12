package com.ssomar.score.sobject;


import com.ssomar.score.features.custom.variables.base.group.VariablesGroupFeature;

/**
 * A SsomarDev object that owns custom variables.
 */
public interface SObjectWithVariables {

    /**
     * Get the variables defined on this object.
     *
     * @return the variables group
     */
    VariablesGroupFeature getVariables();

}
