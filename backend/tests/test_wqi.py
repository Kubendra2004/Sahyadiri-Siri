from __future__ import annotations

from sahyadri_siri.services.wqi import train_model


def test_wqi_scoring_ranges():
    model = train_model()
    high_score = model.predict(5, "Normal", "Low")
    medium_score = model.predict(3, "Normal", "Medium")
    low_score = model.predict(1, "Bad", "High")

    assert 70 <= high_score <= 100
    assert 35 <= medium_score <= 65
    assert 0 <= low_score <= 35
