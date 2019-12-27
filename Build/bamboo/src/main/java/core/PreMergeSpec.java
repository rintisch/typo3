package core;

/*
 * This file is part of the TYPO3 CMS project.
 *
 * It is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License, either version 2
 * of the License, or any later version.
 *
 * For the full copyright and license information, please read the
 * LICENSE.txt file that was distributed with this source code.
 *
 * The TYPO3 project - inspiring people to share!
 */

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.AtlassianModule;
import com.atlassian.bamboo.specs.api.builders.Variable;
import com.atlassian.bamboo.specs.api.builders.notification.AnyNotificationRecipient;
import com.atlassian.bamboo.specs.api.builders.notification.Notification;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.builders.notification.PlanCompletedNotification;
import com.atlassian.bamboo.specs.builders.trigger.RepositoryPollingTrigger;
import com.atlassian.bamboo.specs.util.BambooServer;

/**
 * Core 8.7 pre-merge test plan.
 */
@BambooSpec
public class PreMergeSpec extends AbstractPreMergeSpec {

    private static String planName = "Core 8.7 pre-merge";
    private static String planKey = "GTC87";

    /**
     * Run main to publish plan on Bamboo
     */
    public static void main(final String[] args) throws Exception {
        // By default credentials are read from the '.credentials' file.
        BambooServer bambooServer = new BambooServer(bambooServerName);
        bambooServer.publish(new PreMergeSpec().createPlan());
        bambooServer.publish(new PreMergeSpec().getDefaultPlanPermissions(projectKey, planKey));
    }

    /**
     * Returns full Plan definition
     */
    Plan createPlan() {
        this.isSecurity = false;
        Stage stagePreparation = getPreparationStage();

        Stage stageEarly = getEarlyStage();

        Stage stageMainStage = getMainStage();

        // Compile plan
        return new Plan(project(), planName, planKey)
                .description("Execute TYPO3 core 8.7 pre-merge tests. Auto generated! See Build/bamboo of core git repository.")
                .pluginConfigurations(this.getDefaultPlanPluginConfiguration())
                .stages(
                        stagePreparation,
                        stageEarly,
                        stageMainStage
                )
                .linkedRepositories("github TYPO3 TYPO3.CMS 8.7")
                .triggers(getGerritTrigger())
                .variables(
                        new Variable("changeUrl", ""),
                        new Variable("patchset", "")
                )
                .planBranchManagement(
                        new PlanBranchManagement()
                                .delete(new BranchCleanup())
                                .notificationForCommitters()
                )
                .notifications(new Notification()
                        .type(new PlanCompletedNotification())
                        .recipients(new AnyNotificationRecipient(new AtlassianModule("com.atlassian.bamboo.plugins.bamboo-slack:recipient.slack"))
                                .recipientString("https://intercept.typo3.com/bamboo")
                        )
                );
    }
}
