package com.sasslint.inspection;

import com.sasslint.SassLintBundle;
import com.sasslint.settings.SassLintSettingsPage;
import com.google.common.base.Joiner;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.UnfairLocalInspectionTool;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ex.SingleConfigurableEditor;
import com.intellij.openapi.options.newEditor.OptionsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.List;

public class SassLintInspection extends LocalInspectionTool implements UnfairLocalInspectionTool { //extends PropertySuppressableInspectionBase {

    public static final String INSPECTION_SHORT_NAME = "SassLintInspection";
    public static final Key<SassLintInspection> KEY = Key.create(INSPECTION_SHORT_NAME);

    private static final Logger LOG = Logger.getInstance(SassLintBundle.LOG_ID);

    @NotNull
    public String getDisplayName() {
        return SassLintBundle.message("sasslint.property.inspection.display.name");
    }

    @NotNull
    public String getShortName() {
        return INSPECTION_SHORT_NAME;
    }


    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        return ExternalAnnotatorInspectionVisitor.checkFileWithExternalAnnotator(file, manager, isOnTheFly, new SassLintExternalAnnotator());
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new ExternalAnnotatorInspectionVisitor(holder, new SassLintExternalAnnotator(), isOnTheFly);
    }

    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        HyperlinkLabel settingsLink = createHyperLink();
        panel.setBorder(IdeBorderFactory.createTitledBorder(getDisplayName() + " options"));
        panel.add(settingsLink);
        return panel;
    }

    @NotNull
    public String getId() {
        return "Settings.JavaScript.Linters.SassLint";
    }

    @NotNull
    private HyperlinkLabel createHyperLink() {
//        List path = ContainerUtil.newArrayList(JSBundle.message("settings.javascript.root.configurable.name"), JSBundle.message("settings.javascript.linters.configurable.name"), getDisplayName());
        List path = ContainerUtil.newArrayList("JavaScript", SassLintBundle.message("sasslint.inspection.group.name"), getDisplayName());

        String title = Joiner.on(" / ").join(path);
        final HyperlinkLabel settingsLink = new HyperlinkLabel(title);
        settingsLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void hyperlinkActivated(HyperlinkEvent e) {
                DataContext dataContext = DataManager.getInstance().getDataContext(settingsLink);
                OptionsEditor optionsEditor = OptionsEditor.KEY.getData(dataContext);
                if (optionsEditor == null) {
                    Project project = CommonDataKeys.PROJECT.getData(dataContext);
                    if (project != null) {
                        showSettings(project);
                    }
                    return;
                }
                Configurable configurable = optionsEditor.findConfigurableById(SassLintInspection.this.getId());
                if (configurable != null) {
                    optionsEditor.clearSearchAndSelect(configurable);
                }
            }
        });
        return settingsLink;
    }

    public static void showSettings(Project project) {
        SassLintSettingsPage configurable = new SassLintSettingsPage(project);
        String dimensionKey = ShowSettingsUtilImpl.createDimensionKey(configurable);
        SingleConfigurableEditor singleConfigurableEditor = new SingleConfigurableEditor(project, configurable, dimensionKey, false);
        singleConfigurableEditor.show();
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element) {
        return false;
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getBatchSuppressActions(@Nullable PsiElement element) {
        return new SuppressQuickFix[0];
    }
}