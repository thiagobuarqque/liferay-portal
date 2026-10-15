/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.creator.openai.web.internal.configuration.persistence.listener;

import com.liferay.ai.creator.openai.configuration.AICreatorOpenAIGroupConfiguration;
import com.liferay.ai.creator.openai.web.internal.client.AICreatorOpenAIClient;
import com.liferay.ai.creator.openai.web.internal.exception.AICreatorOpenAIClientException;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.Validator;

import java.util.Dictionary;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Thiago Buarque
 */
@Component(
	property = "model.class.name=com.liferay.ai.creator.openai.configuration.AICreatorOpenAIGroupConfiguration",
	service = ConfigurationModelListener.class
)
public class AICreatorOpenAIGroupConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onBeforeSave(String pid, Dictionary<String, Object> properties)
		throws ConfigurationModelListenerException {

		String apiKey = GetterUtil.getString(properties.get("apiKey"));

		if (Validator.isNull(apiKey)) {
			return;
		}

		try {
			_aiCreatorOpenAIClient.validateAPIKey(apiKey);
		}
		catch (AICreatorOpenAIClientException aiCreatorOpenAIClientException) {
			throw new ConfigurationModelListenerException(
				aiCreatorOpenAIClientException.getLocalizedMessage(
					LocaleThreadLocal.getThemeDisplayLocale()),
				AICreatorOpenAIGroupConfiguration.class, getClass(),
				properties);
		}
		catch (Exception exception) {
			throw new ConfigurationModelListenerException(
				exception, AICreatorOpenAIGroupConfiguration.class, getClass(),
				properties);
		}
	}

	@Reference
	private AICreatorOpenAIClient _aiCreatorOpenAIClient;

}