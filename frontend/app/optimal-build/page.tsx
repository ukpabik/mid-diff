"use client";

import { useState, useEffect } from "react";
import {
  DndContext,
  DragStartEvent,
  DragEndEvent,
  DragOverlay,
  closestCenter,
  useSensor,
  useSensors,
  PointerSensor,
  useDraggable,
  useDroppable,
} from "@dnd-kit/core";
import BackgroundToggle from "@/components/background";
import NavTabs from "@/components/nav-tab";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { ScrollAreaViewport } from "@radix-ui/react-scroll-area";
import { getOptimalBuild } from "@/lib/api";
import { OptimalItemDto } from "@/lib/types";

// Draggable champion icon for pool
function DraggableChampion({
  id,
  champVersion,
}: {
  id: string
  champVersion: string
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    isDragging,
  } = useDraggable({ id })

  return (
    <div
      ref={setNodeRef}
      {...attributes}
      {...listeners}
      style={{
        opacity: isDragging ? 0 : 1,
        width: 48,
        height: 48,
      }}
    >
      <img
        src={`https://ddragon.leagueoflegends.com/cdn/${champVersion}/img/champion/${id}.png`}
        alt={id}
        className="rounded w-full h-full"
      />
    </div>
  )
}

// Droppable slot component
function DroppableSlot({ id, children, className }: { id: string; children: React.ReactNode; className: string }) {
  const { setNodeRef } = useDroppable({ id });
  return (
    <div ref={setNodeRef} id={id} className={className}>
      {children}
    </div>
  );
}

export default function OptimalBuildPage() {
  const [champions, setChampions] = useState<string[]>([]);
  const [champVersion, setChampVersion] = useState<string>("");

  const [yourChampion, setYourChampion] = useState<string | null>(null);
  const [enemyChamps, setEnemyChamps] = useState<(string | null)[]>(Array(5).fill(null));

  const [coreItems, setCoreItems] = useState<OptimalItemDto[]>([]);
  const [itemVersion, setItemVersion] = useState<string>("");

  // track active dragging id
  const [activeId, setActiveId] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      const versions: string[] = await fetch(
        "https://ddragon.leagueoflegends.com/api/versions.json"
      ).then(r => r.json());
      const ver = versions[0];
      setChampVersion(ver);
      const data = await fetch(
        `https://ddragon.leagueoflegends.com/cdn/${ver}/data/en_US/champion.json`
      ).then(r => r.json());
      setChampions(Object.keys(data.data));
    })();
  }, []);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );

  function handleDragStart(event: DragStartEvent) {
    setActiveId(event.active.id as string);
  }

  function handleDragEnd(event: DragEndEvent) {
    setActiveId(null);
    const { active, over } = event;
    if (!over) return;
    const id = active.id as string;
    if (over.id === "your-slot") {
      setYourChampion(id);
    } else if (typeof over.id === "string" && over.id.startsWith("enemy-slot-")) {
      const idx = Number(over.id.split("-")[2]);
      setEnemyChamps(prev => prev.map((c, i) => (i === idx ? id : c)));
    }
  }

  async function handleGenerate() {
    if (!yourChampion) return;
    try{
      const { items, ddragonVersion } = await getOptimalBuild(yourChampion);
      setCoreItems(items);
      setItemVersion(ddragonVersion);
    }
    catch (e) {
      console.error("Could not load optimal build", e)
    }
  }

  return (
    <div className="min-h-screen relative flex flex-col justify-center">
      <BackgroundToggle opacity={30} />
      <NavTabs />

      <Card className="mx-auto mt-16 w-full max-w-2xl bg-gray-800">
        <CardHeader>
          <CardTitle className="text-white">Optimal Build</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex justify-center space-x-3 mb-4">
            {coreItems.map(item => (
              <img
                key={item.id}
                src={`https://ddragon.leagueoflegends.com/cdn/${itemVersion}/img/item/${item.imageFileName}`}
                alt={item.name}
                className="w-12 h-12"
              />
            ))}
          </div>
          <div className="flex justify-center">
            <Button className="cursor-pointer" onClick={handleGenerate}>Generate Optimal Build</Button>
          </div>
        </CardContent>
      </Card>

      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
        onDragCancel={() => setActiveId(null)}
      >
        <div className="mt-8 flex justify-center space-x-12 z-10">
          <DroppableSlot id="your-slot" className="w-16 h-16 sm:w-24 sm:h-24 md:w-32 md:h-32 bg-gray-700 border-2 border-dashed flex items-center justify-center">
            {yourChampion ? (
              <img
                src={`https://ddragon.leagueoflegends.com/cdn/${champVersion}/img/champion/${yourChampion}.png`}
                alt={yourChampion}
                className="w-28 h-28"
              />
            ) : (
              <span className="text-gray-400 text-center">You</span>
            )}
          </DroppableSlot>
          <div className="flex space-x-4">
            {enemyChamps.map((ch, i) => (
              <DroppableSlot
                key={i}
                id={`enemy-slot-${i}`}
                className=" w-12 h-12 sm:w-16 sm:h-16 md:w-24 md:h-24 bg-gray-700 border-2 border-dashed flex items-center justify-center"
              >
                {ch ? (
                  <img
                    src={`https://ddragon.leagueoflegends.com/cdn/${champVersion}/img/champion/${ch}.png`}
                    alt={ch}
                    className="w-20 h-20"
                  />
                ) : (
                  <span className="text-gray-400 text-sm text-center">Enemy {i + 1}</span>
                )}
              </DroppableSlot>
            ))}
          </div>
        </div>
        <div className="mt-8 mx-auto w-full max-w-4xl z-10">
          <Card className="bg-gray-800">
            <CardHeader>
              <CardTitle className="text-white">Champion Pool</CardTitle>
            </CardHeader>
            <CardContent>
            <ScrollArea className="relative w-full h-64">
                <ScrollAreaViewport className="w-full h-full">
                  <div
                    className="
                      grid 
                      grid-cols-4
                      sm:grid-cols-6
                      md:grid-cols-8
                      lg:grid-cols-10
                      gap-2 p-2
                    "
                  >
                    {champions.map(champ => (
                      <DraggableChampion
                        key={champ}
                        id={champ}
                        champVersion={champVersion}
                      />
                    ))}
                  </div>
                </ScrollAreaViewport>

                <ScrollBar orientation="vertical" />
              </ScrollArea>
            </CardContent>
          </Card>
        </div>

        <DragOverlay>
          {activeId ? (
            <img
              src={`https://ddragon.leagueoflegends.com/cdn/${champVersion}/img/champion/${activeId}.png`}
              alt={activeId}
              className="w-12 h-12 opacity-90"
            />
          ) : null}
        </DragOverlay>
      </DndContext>
    </div>
  );
}
