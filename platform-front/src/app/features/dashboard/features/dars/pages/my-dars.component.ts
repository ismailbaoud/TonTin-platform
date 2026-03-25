import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { Subject, takeUntil, finalize } from "rxjs";
import { DarService, Dar } from "../services/dar.service";

// Local interface for display purposes (extends API Dar type)
interface DarDisplay {
  id: string;
  name: string;
  organizer: string;
  organizerAvatar: string;
  imageUrl: string;
  members: number;
  contribution: number;
  potSize: number;
  currentCycle: number;
  totalCycles: number;
  progress: number;
  nextPayout: string;
  isOrganizer: boolean;
  status: string;
  paymentDue: boolean;
  userMemberStatus?: string; // PENDING, ACTIVE, LEAVED
  userPermission?: string; // ORGANIZER, MEMBER
}

@Component({
  selector: "app-my-dars",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./my-dars.component.html",
  styleUrl: "./my-dars.component.scss",
})
export class MyDarsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  searchQuery = "";
  activeTab: "pending" | "active" | "finished" | "all" = "active";
  viewMode: "grid" | "list" = "grid";
  currentPage = 0;
  pageSize = 12;
  totalPages = 0;
  totalElements = 0;
  isLoading = false;
  error: string | null = null;

  // Real data from service (using display interface)
  dars: DarDisplay[] = [];

  constructor(
    private router: Router,
    private darService: DarService,
  ) {}

  ngOnInit(): void {
    this.loadDars();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDars(): void {
    this.isLoading = true;
    this.error = null;

    const status =
      this.activeTab === "all" ? undefined : this.activeTab.toUpperCase();

    this.darService
      .getMyDars(status, this.currentPage, this.pageSize)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (response) => {
          console.log("📊 API Response:", response);
          console.log("📋 Darts received:", response.content.length);

          // Log each dart's user context
          response.content.forEach((dar, index) => {
            console.log(`Dart ${index + 1}: ${dar.name}`);
            console.log(`  - isOrganizer: ${dar.isOrganizer}`);
            console.log(`  - userPermission: ${dar.userPermission}`);
            console.log(`  - userMemberStatus: ${dar.userMemberStatus}`);
          });

          this.dars = this.mapApiDarsToComponent(response.content);
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;

          console.log(
            "✅ Mapped dars:",
            this.dars.map((d) => ({
              name: d.name,
              userMemberStatus: d.userMemberStatus,
              isOrganizer: d.isOrganizer,
            })),
          );
        },
        error: (err) => {
          console.error("Error loading Dârs:", err);
          this.error = "Failed to load your Dârs. Please try again.";
          this.dars = [];
        },
      });
  }

  private mapApiDarsToComponent(apiDars: Dar[]): DarDisplay[] {
    return apiDars.map((dar) => ({
      id: dar.id,
      name: dar.name,
      organizer: dar.isOrganizer ? "You" : dar.organizerName,
      organizerAvatar: dar.organizerAvatar || this.getDefaultAvatar(),
      imageUrl: dar.image || this.getDefaultDarImage(),
      members: dar.totalMembers || dar.memberCount,
      contribution: dar.contributionAmount || dar.monthlyContribution,
      potSize: dar.potSize || dar.totalMonthlyPool,
      currentCycle: dar.currentCycle,
      totalCycles: dar.totalCycles,
      progress:
        dar.totalCycles > 0 ? (dar.currentCycle / dar.totalCycles) * 100 : 0,
      nextPayout: dar.nextPayoutDate || "TBD",
      isOrganizer: dar.isOrganizer,
      status: dar.status,
      paymentDue: false, // TODO: Calculate from payment status
      userMemberStatus: dar.userMemberStatus, // PENDING, ACTIVE, LEAVED
      userPermission: dar.userPermission, // ORGANIZER, MEMBER
    }));
  }

  private getDefaultAvatar(): string {
    return "https://lh3.googleusercontent.com/aida-public/AB6AXuBslXgr8opXf_uENdwJxzyKr9GRCw4G8mfyE8iZQQOngvypPi6214ULvdRZBuUvsIy0WRk3RdepAhh9-RL3tcVRMVvxfL9JUcmM12GxvjNk0oLlZRbTs0oCabm2iHzsq1TLpJG9oDmzaXryRQMyAngNT1Tdf14qsZ59ySzC37XJ8X06OXxX09aAZwtdAq4eTBxG3InSq6T5Drk7Fqk3HIqapsMv96l1zWzUSkStz23i_xUVPiqeKp3MKwHTg3E3eEbdsnnglO56Axs";
  }

  private getDefaultDarImage(): string {
    return "https://lh3.googleusercontent.com/aida-public/AB6AXuD-pwl5GpoIiCkLChiBfM8jBx93qoPfudmizHw4SYrGPAqpLh5iKHnmvode9ncSTcX8DL1gns4F-ae0xNAH2xQWvXgtnTjKu5cagVr0TiEYflQKOxiwOrFxGbm1qTtC_VlRRNYQHS6LHhbRVzoWAn5wWzTm5bGQOLBVHVB9JrBaeYViaKMu-hXjEk-0ZtPSBjmCGFmqrQc9uLkPJD9riQH3WUuNmAKZ5Q8LbrwZ3xayNu7hbn0QqNuKxXqKRl_8x53-0trppiHouNs";
  }

  get filteredDars() {
    // Filter by search query (status filtering is done by API)
    let filtered = this.dars;

    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (dar) =>
          dar.name.toLowerCase().includes(query) ||
          (dar.organizer && dar.organizer.toLowerCase().includes(query)),
      );
    }

    return filtered;
  }

  setTab(tab: "pending" | "active" | "finished" | "all"): void {
    this.activeTab = tab;
    this.currentPage = 0;
    this.loadDars();
  }

  setViewMode(mode: "grid" | "list"): void {
    this.viewMode = mode;
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery = input.value;
  }

  openDetails(darId: string): void {
    console.log("=== Opening Dart Details ===");
    console.log("Dart ID:", darId);
    console.log("Navigation path:", ["/dashboard/client/dar", darId]);
    this.router.navigate(["/dashboard/client/dar", darId]);
  }

  inviteMembers(darId: string): void {
    // Navigate to dar details page where invite functionality exists
    this.router.navigate(["/dashboard/client/dar", darId]);
  }

  editDar(darId: string): void {
    // Navigate to dar details page for editing
    this.router.navigate(["/dashboard/client/dar", darId]);
  }

  leaveDar(darId: string): void {
    if (confirm("Are you sure you want to leave this Dâr?")) {
      this.darService
        .leaveDar(darId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.loadDars();
          },
          error: (err) => {
            console.error("Error leaving Dâr:", err);
            alert("Failed to leave Dâr. Please try again.");
          },
        });
    }
  }

  acceptInvitation(darId: string): void {
    console.log("=== Accepting Invitation ===");
    console.log("Dart ID:", darId);

    this.darService
      .acceptInvitation(darId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log("✅ Invitation accepted successfully");
          // Reload dars to update the status
          this.loadDars();
        },
        error: (err) => {
          console.error("❌ Error accepting invitation:", err);
          alert(
            err.error?.message ||
              "Failed to accept invitation. Please try again.",
          );
        },
      });
  }

  payNow(darId: string): void {
    console.log("Pay now for Dâr:", darId);
    // Navigate to payment page with darId
    this.router.navigate(["/dashboard/client/pay-contribution", darId]);
  }

  createNewDar(): void {
    this.router.navigate(["/dashboard/client/create-dar"]);
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadDars();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadDars();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadDars();
  }
}
