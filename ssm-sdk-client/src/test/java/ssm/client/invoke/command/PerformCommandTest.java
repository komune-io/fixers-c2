package ssm.client.invoke.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import ssm.client.AgentUtils;
import ssm.client.sign.crypto.KeyPairReader;
import ssm.client.sign.model.Signer;
import ssm.dsl.SsmAgentBase;
import ssm.dsl.SsmContextBase;

public class PerformCommandTest {

    @Test
    public void test_execute() throws Exception {

        KeyPair adamPair = KeyPairReader.Companion.loadKeyPair("command/adam");

        Signer signer = new Signer("adam", adamPair);

//        "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0}"
        SsmContextBase context = new SsmContextBase("deal20181201", "100 dollars 1978 Camaro", 0, null);

        InvokeArgs invokeArgs = new PerformCommandSigner(signer, "Sell", context).invoke();
        invokeArgs.getArgs().forEach(System.out::println);

        assertThat(invokeArgs.getFcn()).isEqualTo("perform");
        assertThat(invokeArgs.getArgs())
                .isNotEmpty()
                .containsExactly(
                        "Sell",
                        "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0}",
                        "adam",
                        "GOqqfALK7M682IFEBU66GG8aIevDc5mGa3W/o7UXhsvweliz/1XWxWdNOfcEvMJ9DnfhrX1Bupyo956EliiFmutcbi0Hvp+IuzgjbcKUtY2jAGKWA8QLmBeN2DGnYMgor4aJiOZKS+NnA3coNz/usHalIAzqZlihq41ZANpr1Uqjc728CNXUOVuIeRJ0oWRo8X18SO0o+VRB+/0+BzGexu6In/zcoaEjDlMBtB29U0hFgB1DX28Ek0snuZYMsacL76MpKXZjr8v2Sc1oQ63PSUSrjSmCzni0lZ28AT4VJoui2LU2FKLsCqKLKyi+pIP82a+9ahdmwIQ5r0X4dpGCIQ=="
                );

    }

    @Test
    public void test_executeWithPrivateMessage() throws Exception {

        KeyPair adamPair = KeyPairReader.Companion.loadKeyPair("command/adam");

        Signer signer = new Signer("adam", adamPair);
        SsmAgentBase agent = AgentUtils.loadFromFile("vivi", "command/adam");

//        "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0}"
        SsmContextBase context = new SsmContextBase("deal20181201", "100 dollars 1978 Camaro", 0,   ImmutableMap.of("vivi", "message"));

        InvokeArgs invokeArgs = new PerformCommandSigner(signer, "Sell", context).invoke();
        invokeArgs.getArgs().forEach(System.out::println);

        assertThat(invokeArgs.getFcn()).isEqualTo("perform");
        assertThat(invokeArgs.getArgs())
                .isNotEmpty()
                .containsExactly(
                        "Sell",
                        "{\"session\":\"deal20181201\",\"public\":\"100 dollars 1978 Camaro\",\"iteration\":0,\"private\":{\"vivi\":\"message\"}}",
                        "adam",
                        "mBWkUXlR+4WKYDU6ZWur7bRjnal2jfccb9GQpvNwOiYxQ0XuRiqExHOyBNt1zyGU0qRzkEuGWAEGhsnHBQEjLeVlF+Dkf92llp/NwBymN3N/hPd6ha6M0OPkSUeu+kqbUuhVw7GqvoqBo0A5qkbeKd8ue/lbfi9bhJXTxOLzIEanjZqNnfTcmkkSZFtv3FsDQ4fbt6BZfOGpoLn3HcJZP1lTPROlLbuxzD+YdF7pBEQDL3S8NVIHGHwK7RAaxPrazABy1VdJUlfnScpk8VjiVxUlAeGu985s/6N7Zv8sF/0c7r4b0Pjg7koiasMFuafNOWVpTc2tTp/4AqHuS4fKAw=="
                );

    }

}