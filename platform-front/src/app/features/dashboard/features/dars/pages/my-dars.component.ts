import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { Subject, takeUntil, finalize } from "rxjs";
import { DarService, Dar } from "../services/dar.service";

// Local interface for display purposes (extends API Dar type)
interface DarDisplay {
  id: number;
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
  activeTab: "active" | "completed" | "all" = "active";
  viewMode: "grid" | "list" = "grid";
  currentPage = 0;
  pageSize = 12;
  totalPages = 0;
  totalElements = 0;
  isLoading = false;
  error: string | null = null;

  // Real data from service (using display interface)
  dars: DarDisplay[] = [];

  // Keep mock data as fallback for now
  mockDars: DarDisplay[] = [
    {
      id: 1,
      name: "Family Vacation Fund",
      organizer: "You",
      organizerAvatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBslXgr8opXf_uENdwJxzyKr9GRCw4G8mfyE8iZQQOngvypPi6214ULvdRZBuUvsIy0WRk3RdepAhh9-RL3tcVRMVvxfL9JUcmM12GxvjNk0oLlZRbTs0oCabm2iHzsq1TLpJG9oDmzaXryRQMyAngNT1Tdf14qsZ59ySzC37XJ8X06OXxX09aAZwtdAq4eTBxG3InSq6T5Drk7Fqk3HIqapsMv96l1zWzUSkStz23i_xUVPiqeKp3MKwHTg3E3eEbdsnnglO56Axs",
      imageUrl:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuD-pwl5GpoIiCkLChiBfM8jBx93qoPfudmizHw4SYrGPAqpLh5iKHnmvode9ncSTcX8DL1gns4F-ae0xNAH2xQWvXgtnTjKu5cagVr0TiEYflQKOxiwOrFxGbm1qTtC_VlRRNYQHS6LHhbRVzoWAn5wWzTm5bGQOLBVHVB9JrBaeYViaKMu-hXjEk-0ZtPSBjmCGFmqrQc9uLkPJD9riQH3WUuNmAKZ5Q8LbrwZ3xayNu7hbn0QqNuKxXqKRl_8x53-0trppiHouNs",
      members: 10,
      contribution: 200,
      potSize: 2000,
      currentCycle: 9,
      totalCycles: 12,
      progress: 75,
      nextPayout: "Oct 15, 2023",
      isOrganizer: true,
      status: "active",
      paymentDue: false,
    },
    {
      id: 2,
      name: "Office Savings",
      organizer: "Sarah M.",
      organizerAvatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAX0GIk0C6xkGB2qg6Yaosu9BD-CR4mMygfR3JjwA3vR3m2slfIOEFXJ2M0IZsMoadZk9zoZ1XmHexlYRGneawAadMhiK_YIQIDuxuNRRV4-s5NPauHcHqVAbFmqH8u2v58ju61TQehR4z57aE8zopqNjkHnqT7d0JN4iAPgahfKq-5ID-PQqWhc7zg4vE5qWDbcGw-GDnvfwKGRMqti7Cr6bRxVUm4pJ2ahRNCdgnkvVvld12ccT6m6vu7wwS4YVINoJ1pKP4rTAA",
      imageUrl:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCvPZkT9HKEki3bacKt4uZr-sWBG0K73zYVecKoFFcrx2k2NIQt0P2B682f0TsA4uTAy8J1r2Nc8NHbN5tRjS6VyUuNznxDgYXJFW_uGrFES6udJ7noIfc0FwFvWyXy-dqmWOerIr_KYdFuRp4Qxk046WkchTz6UwUZEPlNl3KIsgcJfuawaxTpvhAkiO7HNlfEbGRhqjARIxFzk7w1R59U4bLAfM2VLFN3EthlWqyKBOj29FQuwTztBhZkG7yNYx4e00K2oWXY-2U",
      members: 6,
      contribution: 500,
      potSize: 3000,
      currentCycle: 2,
      totalCycles: 6,
      progress: 33,
      nextPayout: "Nov 01, 2023",
      isOrganizer: false,
      status: "active",
      paymentDue: false,
    },
    {
      id: 3,
      name: "New Car Fund",
      organizer: "John D.",
      organizerAvatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCa_UIxeCAlZmIJa3MODfVMK_D331PKTXF5Wx6r0q0rK4Re8_28bZqAHImLoFesrCOl6BBlV7lYhHkYA9crC1Z8OPKRCv3sykCgBrtdZCLvhkfD3r8zenLsw_VdcweubEJ6gO-6FyNbeU6EyDZsXqcYMsDIiQOSnoxIUO1zZpd4JzbmYGuAWNvuiC-hc5QXyMSJdy7XtJkcLdV5XiaWOJz12o_vo64yvuHGw_hOyyYypnAxT1Jln1O-8TKLFfYiwVnDQ4V99yZhpDU",
      imageUrl:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCqVi8UQF4ICIpC4VeAYoJUY2OHMreTMyqusLXu11OD-KUq6Zc9LMAPQP4mQoDHsIhRiiNx-SCi1hoyQnYV0YumxCUAml25inVe1DV7REDo6NHNznd8C7MMaIzgBanycRElYZ2avfAVKnkpROuCNVAbovcWBK9rBhtrJEiWGKB6E_22zwhSm7YNtt2dlvME5KaMuBTylryCZPm-SrID-N0bhL7trUmEInW8SZxRcEZhqlfx0XIdDKFBeij0cK_zUeUOWJYOdo0rFG8",
      members: 12,
      contribution: 150,
      potSize: 1800,
      currentCycle: 6,
      totalCycles: 12,
      progress: 50,
      nextPayout: "Yesterday",
      isOrganizer: false,
      status: "late",
      paymentDue: true,
    },
    {
      id: 4,
      name: "Community Trust",
      organizer: "You",
      organizerAvatar:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAxolJMHf35wfCH7vY6xCEyf2Gx3zpoD1YK7XmkUWub7SwkVkedAArk_iWYll7nm2xNQiG2h0C0wnXuA0uOyueLbejmE6XMSC6gsYPexU4_giNyVoYOkxwyTteT19oFXVomkjn1Zpc_wTBrtE_kD70SJAUk6YFBZ3p3734MGXIY3t6-KKxhAO8K4LzlrFyNCNVKTEqQhuSLCkTl6xCehozObLrvNWQPvgsvbdBSnZ9GV0ld8TRXi2VchSRq8heW0zdSSVNeGGVlTsc",
      imageUrl:
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBx2kNhujOex24bGM7iRK0uOBtlkbgXOperviXvIWMSnS6pB9YZf05R9SG16X-XTgt3aJzAbbGdq_fgKJW0N3NdEkZFyx44CwUjx_hwgNJ-Hrsq-vLVKquqgwAJLkfr8aaJtkZoYX31GThFDWbKKchupEMh8iu7Om1fy8ulhLCwVDBKIuxnNBzEGEBARJzkLQCRrg_QcQ161JGpqujWk8x2Mab3YQf4jZTtkp7oNBRMSc48IwvPP0evxuCI7A74kxN0Hlc5np8DSqM",
      members: 20,
      contribution: 100,
      potSize: 2000,
      currentCycle: 1,
      totalCycles: 20,
      progress: 5,
      nextPayout: "Nov 15, 2023",
      isOrganizer: true,
      status: "active",
      paymentDue: false,
    },
  ];

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

    const status = this.activeTab === "all" ? undefined : this.activeTab;

    this.darService
      .getMyDars(status, this.currentPage, this.pageSize)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (response) => {
          this.dars = this.mapApiDarsToComponent(response.content);
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
        },
        error: (err) => {
          console.error("Error loading Dârs:", err);
          this.error = "Failed to load your Dârs. Please try again.";
          // Fallback to mock data on error
          this.dars = this.mockDars;
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
      members: dar.totalMembers,
      contribution: dar.contributionAmount,
      potSize: dar.potSize,
      currentCycle: dar.currentCycle,
      totalCycles: dar.totalCycles,
      progress:
        dar.totalCycles > 0 ? (dar.currentCycle / dar.totalCycles) * 100 : 0,
      nextPayout: dar.nextPayoutDate || "TBD",
      isOrganizer: dar.isOrganizer,
      status: dar.status,
      paymentDue: false, // TODO: Calculate from payment status
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

  setTab(tab: "active" | "completed" | "all"): void {
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

  openDetails(darId: number): void {
    this.router.navigate(["/dashboard/client/dar", darId]);
  }

  inviteMembers(darId: number): void {
    // Navigate to dar details page where invite functionality exists
    this.router.navigate(["/dashboard/client/dar", darId]);
  }

  editDar(darId: number): void {
    // Navigate to dar details page for editing
    this.router.navigate(["/dashboard/client/dar", darId]);
  }

  leaveDar(darId: number): void {
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

  payNow(darId: number): void {
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
