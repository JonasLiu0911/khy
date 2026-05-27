from __future__ import annotations

from typing import Any
from pydantic import BaseModel, Field


class StationTime(BaseModel):
    station: str
    upper_times: list[str] = Field(default_factory=list)
    lower_times: list[str] = Field(default_factory=list)

class OCRBox(BaseModel):
    text: str
    score: float = 0.0
    box: list[list[float]] = Field(default_factory=list)

    @property
    def x1(self) -> float:
        return min(p[0] for p in self.box) if self.box else 0.0

    @property
    def y1(self) -> float:
        return min(p[1] for p in self.box) if self.box else 0.0

    @property
    def x2(self) -> float:
        return max(p[0] for p in self.box) if self.box else 0.0

    @property
    def y2(self) -> float:
        return max(p[1] for p in self.box) if self.box else 0.0

    @property
    def cx(self) -> float:
        return (self.x1 + self.x2) / 2

    @property
    def cy(self) -> float:
        return (self.y1 + self.y2) / 2

    @property
    def height(self) -> float:
        return self.y2 - self.y1

    @property
    def width(self) -> float:
        return self.x2 - self.x1


class DirectionTable(BaseModel):
    direction: str = ""
    trips: list[list[str]] = Field(default_factory=list)


class RouteExtraction(BaseModel):
    route_name: str | None = None
    stations: list[str] = Field(default_factory=list)
    forward_direction: DirectionTable = Field(default_factory=DirectionTable)
    reverse_direction: DirectionTable = Field(default_factory=DirectionTable)
    frequency: str | None = None
    operation_time: str | None = None
    phone: str | None = None
    fares: dict[str, str] = Field(default_factory=dict)
    confidence_notes: list[str] = Field(default_factory=list)
    raw_text_lines: list[str] = Field(default_factory=list)
    debug: dict[str, Any] = Field(default_factory=dict)
    station_times: list[dict] = []
