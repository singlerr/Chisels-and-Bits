package com.communi.suggestu.formula.core;

import com.communi.suggestu.formula.core.registrars.ItemRegistrar;

public class Formula
{
    public Formula()
    {
        ItemRegistrar.onModConstruction();
    }
}
