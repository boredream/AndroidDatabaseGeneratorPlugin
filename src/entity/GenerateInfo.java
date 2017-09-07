package entity;

import com.intellij.psi.PsiField;

import java.util.ArrayList;

public class GenerateInfo {

    private ArrayList<PsiField> fields;
    private PsiField priKeyFields;

    public ArrayList<PsiField> getFields() {
        return fields;
    }

    public void setFields(ArrayList<PsiField> fields) {
        this.fields = fields;
    }

    public PsiField getPriKeyFields() {
        return priKeyFields;
    }

    public void setPriKeyFields(PsiField priKeyFields) {
        this.priKeyFields = priKeyFields;
    }
}
