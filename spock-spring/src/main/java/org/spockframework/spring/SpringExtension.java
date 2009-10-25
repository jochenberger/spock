/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring;

import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import org.spockframework.runtime.extension.ISpockExtension;
import org.spockframework.runtime.model.*;

import spock.lang.Shared;

public class SpringExtension implements ISpockExtension {
  public void visitSpeck(SpeckInfo speck) {
    if (!speck.getReflection().isAnnotationPresent(ContextConfiguration.class)) return;

    checkNoSharedFieldInjected(speck);

    TestContextManager manager = new TestContextManager(speck.getReflection());
    SpringInterceptor interceptor = new SpringInterceptor(manager);
    speck.addInterceptor(interceptor);
    speck.getSetupMethod().addInterceptor(interceptor);
    speck.getCleanupMethod().addInterceptor(interceptor);
    for (FeatureInfo feature : speck.getFeatures())
      feature.addInterceptor(interceptor);
  }

  private void checkNoSharedFieldInjected(SpeckInfo speck) {
    for (FieldInfo field : speck.getFields()) {
      Field reflection = field.getReflection();
      if (reflection.isAnnotationPresent(Shared.class)
          && (reflection.isAnnotationPresent(Autowired.class)
          || reflection.isAnnotationPresent(Resource.class)))
        throw new SpringExtensionException(
            "@Shared field '%s' cannot be injected; use an instance field instead").format(field.getName());
    }
  }
}