/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.editor;

import com.intellij.psi.tree.TokenSet;
import com.jetbrains.python.PyTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author traff
 */
public class PythonQuoteHandler extends BaseQuoteHandler {
  public PythonQuoteHandler() {
    super(PyTokenTypes.STRING_NODES, new char[]{'}', ']', ')', ',', ':', ';', ' ', '\t', '\n'});
  }

  @NotNull
  @Override
  protected TokenSet getOpeningQuotesTokens() {
    return TokenSet.orSet(super.getOpeningQuotesTokens(), TokenSet.create(PyTokenTypes.FSTRING_START));
  }

  @NotNull
  @Override
  protected TokenSet getClosingQuotesTokens() {
    return TokenSet.orSet(super.getClosingQuotesTokens(), TokenSet.create(PyTokenTypes.FSTRING_END));
  }

  @NotNull
  @Override
  protected TokenSet getLiteralContentTokens() {
    return TokenSet.orSet(super.getLiteralContentTokens(), TokenSet.create(PyTokenTypes.FSTRING_TEXT));
  }
}
