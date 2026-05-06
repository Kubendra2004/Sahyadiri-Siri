from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Any

import joblib
import numpy as np
from sklearn.ensemble import RandomForestRegressor

SMELL_MAP = {"Normal": 0, "Bad": 1}
FLOW_MAP = {"Low": 0, "Medium": 1, "High": 2}


def encode_smell(smell: str) -> int:
    return SMELL_MAP.get(smell, 0)


def encode_flow(flow: str) -> int:
    return FLOW_MAP.get(flow, 1)


def _seeded_rng(seed: int = 42) -> np.random.Generator:
    return np.random.default_rng(seed)


def generate_training_samples(sample_count: int = 500, seed: int = 42) -> tuple[np.ndarray, np.ndarray]:
    rng = _seeded_rng(seed)
    features: list[list[float]] = []
    targets: list[float] = []
    for _ in range(sample_count):
        clarity = int(rng.integers(1, 6))
        smell = rng.choice(["Normal", "Bad"])
        flow = rng.choice(["Low", "Medium", "High"])

        if clarity >= 4 and smell == "Normal":
            target = float(rng.uniform(70, 100))
        elif clarity in {2, 3} and smell == "Normal":
            target = float(rng.uniform(35, 65))
        else:
            target = float(rng.uniform(0, 35))

        target += (encode_flow(flow) - 1) * float(rng.uniform(-3, 3))
        target = float(np.clip(target, 0, 100))
        features.append([float(clarity), float(encode_smell(smell)), float(encode_flow(flow))])
        targets.append(target)

    return np.asarray(features, dtype=float), np.asarray(targets, dtype=float)


@dataclass(slots=True)
class WQIModel:
    model: Any

    def predict(self, clarity: int, smell: str, flow: str) -> float:
        features = np.asarray([[float(clarity), float(encode_smell(smell)), float(encode_flow(flow))]], dtype=float)
        prediction = float(self.model.predict(features)[0])
        return float(np.clip(prediction, 0, 100))


def train_model(sample_count: int = 500, seed: int = 42) -> WQIModel:
    x_train, y_train = generate_training_samples(sample_count=sample_count, seed=seed)
    model = RandomForestRegressor(n_estimators=200, random_state=seed)
    model.fit(x_train, y_train)
    return WQIModel(model=model)


def load_wqi_model(model_path: str | Path) -> WQIModel:
    path = Path(model_path)
    if path.exists():
        loaded = joblib.load(path)
        return WQIModel(model=loaded)
    return train_model()
