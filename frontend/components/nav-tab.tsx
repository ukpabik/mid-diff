"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem } from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Menu as MenuIcon } from "lucide-react";


export default function NavTabs() {
  const pathname = usePathname();
  const active = pathname === "/" ? "search" : "optimal";

  return (
    <div className="fixed top-0 left-0 z-20 p-4">
      <div className="flex items-center space-x-2">
        <div className="hidden sm:block bg-gray-800 rounded">
          <Tabs defaultValue={active} className="">
            <TabsList>
              <TabsTrigger value="search">
                <Link href="/">Search</Link>
              </TabsTrigger>
              <TabsTrigger value="optimal">
                <Link href="/optimal-build">Builds</Link>
              </TabsTrigger>
            </TabsList>
          </Tabs>
        </div>

        <div className="sm:hidden">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="p-2">
                <MenuIcon className="w-6 h-6 text-white" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent side="bottom" align="start">
              <DropdownMenuItem asChild>
                <Link href="/">Search</Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild>
                <Link href="/optimal-build">Builds</Link>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </div>
  );
}