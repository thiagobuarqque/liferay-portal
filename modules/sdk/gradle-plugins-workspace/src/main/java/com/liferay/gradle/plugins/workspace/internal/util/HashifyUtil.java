/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.gradle.plugins.workspace.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thiago Buarque
 */
public class HashifyUtil {

	public static String[] getURLKeys(String type) {
		return _urlKeysMap.get(type);
	}

	public static boolean isHashedFileName(String fileName) {
		Matcher matcher = _hashedFileNamePattern.matcher(fileName);

		return matcher.matches();
	}

	public static boolean isHashifyRequired(Map<String, Object> typeSettings) {
		return !Objects.equals(
			String.valueOf(typeSettings.get("hashifyRequired")), "false");
	}

	private static final Pattern _hashedFileNamePattern = Pattern.compile(
		".+\\.[0-9a-f]{32,40}(\\.[^.]+)?");
	private static final Map<String, String[]> _urlKeysMap =
		new HashMap<String, String[]>() {
			{
				put("globalCSS", new String[] {"url"});
				put(
					"themeCSS",
					new String[] {
						"clayRTLURL", "clayURL", "mainRTLURL", "mainURL"
					});
				put("themeFavicon", new String[] {"url"});
				put("themeSpritemap", new String[] {"url"});
			}
		};

}