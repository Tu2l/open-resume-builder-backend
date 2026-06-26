package com.tu2l.common.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.accept.SemanticApiVersionParser;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixedSemanticApiVersionParserTest {

    private final PrefixedSemanticApiVersionParser parser = new PrefixedSemanticApiVersionParser();
    private final SemanticApiVersionParser plain = new SemanticApiVersionParser();

    @Test
    void stripsLeadingLowercaseV() {
        assertThat(parser.parseVersion("v1")).isEqualTo(plain.parseVersion("1"));
    }

    @Test
    void stripsLeadingUppercaseV() {
        assertThat(parser.parseVersion("V2.3")).isEqualTo(plain.parseVersion("2.3"));
    }

    @Test
    void passesThroughVersionWithoutPrefix() {
        assertThat(parser.parseVersion("1.4.2")).isEqualTo(plain.parseVersion("1.4.2"));
    }

    @Test
    void onlyStripsTheSingleLeadingPrefixCharacter() {
        // A second character that happens to be a digit must be preserved.
        assertThat(parser.parseVersion("v12")).isEqualTo(plain.parseVersion("12"));
    }
}
