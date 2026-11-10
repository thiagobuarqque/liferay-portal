/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.product.navigation.applications.menu.web.internal.display.context.test;

import com.liferay.application.list.BasePanelApp;
import com.liferay.application.list.PanelApp;
import com.liferay.application.list.PanelAppRegistry;
import com.liferay.application.list.constants.ApplicationListWebKeys;
import com.liferay.application.list.constants.PanelCategoryKeys;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.module.util.BundleUtil;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.product.navigation.applications.menu.web.internal.display.context.test.portlet.HomeDisplayContextTestPortlet;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Constructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Gabriel Prates
 */
@RunWith(Arquillian.class)
public class HomeDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(HomeDisplayContextTest.class);

		_bundleContext = bundle.getBundleContext();

		bundle = BundleUtil.getBundle(
			_bundleContext,
			"com.liferay.product.navigation.applications.menu.web");

		Class<?> clazz = bundle.loadClass(
			"com.liferay.product.navigation.applications.menu.web.internal." +
				"display.context.HomeDisplayContext");

		_constructor = clazz.getConstructor(HttpServletRequest.class);

		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testGetProps() throws Exception {
		_testGetProps("/" + RandomTestUtil.randomString(), true);
		_testGetProps(
			"data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org" +
				"%2F2000%2Fsvg%22%2F%3E",
			true);
		_testGetProps("data:image/svg+xml;charset=utf-8,%3Csvg%2F%3E", true);
		_testGetProps(
			"https://" + RandomTestUtil.randomString() + ".com/icon.png", true);

		_testGetProps(null, false);
		_testGetProps("data:text/html,<script>alert(1)</script>", false);
		_testGetProps("javascript:alert(1)", false);
	}

	private HttpServletRequest _getHttpServletRequest() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			ApplicationListWebKeys.PANEL_APP_REGISTRY, _panelAppRegistry);

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(_group.getCompanyId()));
		themeDisplay.setLayout(LayoutTestUtil.addTypePortletLayout(_group));
		themeDisplay.setLocale(
			LocaleUtil.fromLanguageId(_group.getDefaultLanguageId()));
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(TestPropsValues.getUser()));
		themeDisplay.setPpid(
			"com_liferay_applications_home_web_internal_portlet_" +
				"ApplicationsHomePortlet");
		themeDisplay.setRequest(mockHttpServletRequest);
		themeDisplay.setScopeGroupId(_group.getGroupId());
		themeDisplay.setSignedIn(true);
		themeDisplay.setSiteGroupId(_group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		return mockHttpServletRequest;
	}

	private Map<String, Object> _getPropsItem() throws Exception {
		Map<String, Object> props = ReflectionTestUtil.invoke(
			_constructor.newInstance(_getHttpServletRequest()), "getProps",
			new Class<?>[0]);

		for (Map<String, Object> groupPropsItem :
				(List<Map<String, Object>>)props.get("items")) {

			for (Map<String, Object> propsItem :
					(List<Map<String, Object>>)groupPropsItem.get("items")) {

				if (Objects.equals(
						propsItem.get("id"),
						HomeDisplayContextTestPortlet.PORTLET_NAME)) {

					return propsItem;
				}
			}
		}

		return null;
	}

	private void _testGetProps(String applicationIconURL, boolean valid)
		throws Exception {

		ServiceRegistration<PanelApp> serviceRegistration =
			_bundleContext.registerService(
				PanelApp.class,
				new TestPanelApp(
					applicationIconURL,
					_portletLocalService.getPortletById(
						HomeDisplayContextTestPortlet.PORTLET_NAME)),
				HashMapDictionaryBuilder.<String, Object>put(
					"panel.app.order", RandomTestUtil.randomInt()
				).put(
					"panel.category.key",
					PanelCategoryKeys.APPLICATIONS_MENU_APPLICATIONS
				).build());

		try {
			Map<String, Object> propsItem = _getPropsItem();

			Assert.assertNotNull(propsItem);

			if (valid) {
				Assert.assertEquals(
					applicationIconURL, propsItem.get("applicationIconURL"));
			}
			else {
				Assert.assertFalse(propsItem.containsKey("applicationIconURL"));
			}
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private BundleContext _bundleContext;

	@Inject
	private CompanyLocalService _companyLocalService;

	private Constructor<?> _constructor;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private PanelAppRegistry _panelAppRegistry;

	@Inject
	private PortletLocalService _portletLocalService;

	private static class TestPanelApp extends BasePanelApp {

		public TestPanelApp(String applicationIconURL, Portlet portlet) {
			_applicationIconURL = applicationIconURL;
			_portlet = portlet;
		}

		@Override
		public String getApplicationIconURL() {
			return _applicationIconURL;
		}

		@Override
		public Portlet getPortlet() {
			return _portlet;
		}

		@Override
		public String getPortletId() {
			return HomeDisplayContextTestPortlet.PORTLET_NAME;
		}

		private final String _applicationIconURL;
		private final Portlet _portlet;

	}

}