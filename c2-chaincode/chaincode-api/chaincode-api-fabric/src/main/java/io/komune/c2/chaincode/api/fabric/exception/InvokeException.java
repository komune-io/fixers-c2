package io.komune.c2.chaincode.api.fabric.exception;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import java.util.List;

public class InvokeException extends Exception {

    public InvokeException(String error) {
        super(error);
    }

    public InvokeException(String error, Exception e) {
        super(error, e);
    }

    public InvokeException(List<String> errors) {
        super(Joiner.on(";").join(Sets.newHashSet(errors)));
    }

    public InvokeException(Exception e) {
        super(e);
    }

}
