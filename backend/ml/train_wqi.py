from __future__ import annotations

from pathlib import Path

import joblib

from sahyadri_siri.services.wqi import train_model


def main() -> None:
    model = train_model()
    output_path = Path(__file__).resolve().parents[1] / "src" / "sahyadri_siri" / "ml" / "wqi_model.pkl"
    output_path.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(model.model, output_path)
    print(f"Saved WQI model to {output_path}")


if __name__ == "__main__":
    main()
