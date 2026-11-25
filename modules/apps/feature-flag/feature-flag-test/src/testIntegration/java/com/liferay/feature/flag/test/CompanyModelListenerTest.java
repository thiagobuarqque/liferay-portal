/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.feature.flag.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlag;
import com.liferay.portal.kernel.feature.flag.FeatureFlagListener;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManager;
import com.liferay.portal.kernel.feature.flag.FeatureFlagType;
import com.liferay.portal.kernel.feature.flag.constants.FeatureFlagConstants;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.service.PortalPreferencesLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portlet.PortalPreferencesWrapper;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.promise.Promise;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Thiago Buarque
 */
@RunWith(Arquillian.class)
public class CompanyModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() {
		Bundle bundle = FrameworkUtil.getBundle(CompanyModelListenerTest.class);

		_bundleContext = bundle.getBundleContext();
	}

	@Test
	public void testDeprecationFeatureFlags() throws Exception {
		PortalPreferencesWrapper portalPreferencesWrapper =
			(PortalPreferencesWrapper)
				_portalPreferencesLocalService.getPreferences(
					CompanyConstants.SYSTEM,
					PortletKeys.PREFS_OWNER_TYPE_COMPANY);

		PortalPreferences portalPreferences =
			portalPreferencesWrapper.getPortalPreferencesImpl();

		Assert.assertEquals(
			"true",
			portalPreferences.getValue(
				FeatureFlagConstants.PREFERENCE_NAMESPACE,
				FeatureFlagConstants.PREFERENCE_KEY_DEPRECATION_PROCESSED));

		Company company = CompanyTestUtil.addCompany();

		portalPreferencesWrapper =
			(PortalPreferencesWrapper)
				_portalPreferencesLocalService.getPreferences(
					company.getCompanyId(),
					PortletKeys.PREFS_OWNER_TYPE_COMPANY);

		portalPreferences = portalPreferencesWrapper.getPortalPreferencesImpl();

		List<FeatureFlag> deprecationFeatureFlags =
			_featureFlagManager.getFeatureFlags(
				company.getCompanyId(),
				FeatureFlagType.DEPRECATION.getPredicate());

		for (FeatureFlag deprecationFeatureFlag : deprecationFeatureFlags) {
			Assert.assertEquals(
				"false",
				portalPreferences.getValue(
					FeatureFlagConstants.PREFERENCE_NAMESPACE,
					deprecationFeatureFlag.getKey()));
			Assert.assertFalse(deprecationFeatureFlag.isEnabled());
		}
	}

	@Test
	public void testGetServiceWhenFeatureFlagsBagProviderIsActivating()
		throws Exception {

		AtomicBoolean activating = new AtomicBoolean();
		AtomicReference<Object> companyModelListenerAtomicReference =
			new AtomicReference<>();
		CountDownLatch countDownLatch = new CountDownLatch(1);

		ServiceRegistration<FeatureFlagListener> serviceRegistration =
			_bundleContext.registerService(
				FeatureFlagListener.class,
				(companyId, featureFlagKey, enabled) -> {
					if (!activating.get() || (countDownLatch.getCount() == 0)) {
						return;
					}

					companyModelListenerAtomicReference.set(
						_getCompanyModelListener());

					countDownLatch.countDown();
				},
				HashMapDictionaryBuilder.<String, Object>put(
					"feature.flag.key", "*"
				).build());

		try {
			activating.set(true);

			_reactivateFeatureFlagsBagProvider();

			Assert.assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));
			Assert.assertNotNull(companyModelListenerAtomicReference.get());
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private Object _getCompanyModelListener() {
		try {
			ServiceReference<?>[] serviceReferences =
				_bundleContext.getServiceReferences(
					ModelListener.class.getName(),
					"(component.name=com.liferay.feature.flag.web.internal." +
						"model.listener.CompanyModelListener)");

			if (serviceReferences == null) {
				return null;
			}

			try {
				return _bundleContext.getService(serviceReferences[0]);
			}
			finally {
				_bundleContext.ungetService(serviceReferences[0]);
			}
		}
		catch (InvalidSyntaxException invalidSyntaxException) {
			return ReflectionUtil.throwException(invalidSyntaxException);
		}
	}

	private void _reactivateFeatureFlagsBagProvider() throws Exception {
		ServiceReference<?> serviceReference =
			_bundleContext.getServiceReference(
				_CLASS_NAME_FEATURE_FLAGS_BAG_PROVIDER);

		ComponentDescriptionDTO componentDescriptionDTO =
			_serviceComponentRuntime.getComponentDescriptionDTO(
				serviceReference.getBundle(),
				_CLASS_NAME_FEATURE_FLAGS_BAG_PROVIDER + "Impl");

		Promise<Void> promise = _serviceComponentRuntime.disableComponent(
			componentDescriptionDTO);

		promise.getValue();

		promise = _serviceComponentRuntime.enableComponent(
			componentDescriptionDTO);

		promise.getValue();

		ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<>(
			_bundleContext, _CLASS_NAME_FEATURE_FLAGS_BAG_PROVIDER, null);

		serviceTracker.open(true);

		try {
			serviceTracker.waitForService(10000);
		}
		finally {
			serviceTracker.close();
		}
	}

	private static final String _CLASS_NAME_FEATURE_FLAGS_BAG_PROVIDER =
		"com.liferay.feature.flag.web.internal.feature.flag." +
			"FeatureFlagsBagProvider";

	private BundleContext _bundleContext;

	@Inject
	private FeatureFlagManager _featureFlagManager;

	@Inject
	private PortalPreferencesLocalService _portalPreferencesLocalService;

	@Inject
	private ServiceComponentRuntime _serviceComponentRuntime;

}